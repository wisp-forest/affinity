package io.wispforest.affinity.client.screen;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.VillagerArmatureBlock;
import io.wispforest.affinity.blockentity.impl.VillagerArmatureBlockEntity;
import io.wispforest.affinity.client.misc.Interpolator;
import io.wispforest.affinity.client.misc.WorldMeshUtil;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.mixin.client.CameraInvoker;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.worldmesher.WorldMesh;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.wispforest.affinity.client.screen.FluxNetworkVisualizerScreen.VISUALIZER_BUFFER;

public class VillagerArmatureScreen extends BaseUIModelScreen<FlowLayout> {

    public static final Identifier CROSSHAIR_MODEL_ID = Affinity.id("villager_armature_crosshair");
    public static final Identifier CROSSHAIR_PREVIEW_MODEL_ID = Affinity.id("villager_armature_crosshair_preview");

    private final VillagerArmatureBlockEntity armature;
    private final MeshesComponent meshes;

    public VillagerArmatureScreen(VillagerArmatureBlockEntity armature) {
        super(FlowLayout.class, Affinity.id("villager_armature"));
        this.armature = armature;

        this.meshes = new MeshesComponent(armature);
        this.meshes.sizing(Sizing.fixed(160), Sizing.fixed(140));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(StackLayout.class, "meshes-anchor").child(this.meshes);

        this.setupOptionButton(
            "action",
            $ -> this.armature.action = VillagerArmatureBlockEntity.Action.values()[(this.armature.action.ordinal() + 1) % VillagerArmatureBlockEntity.Action.values().length],
            () -> this.armature.action.name().toLowerCase(Locale.ROOT)
        );

        this.setupOptionButton(
            "redstone-mode",
            $ -> this.armature.redstoneMode = VillagerArmatureBlockEntity.RedstoneMode.values()[(this.armature.redstoneMode.ordinal() + 1) % VillagerArmatureBlockEntity.RedstoneMode.values().length],
            () -> this.armature.redstoneMode.name().toLowerCase(Locale.ROOT)
        );

        this.setupOptionButton(
            "sneak",
            $ -> this.armature.sneak = !this.armature.sneak,
            () -> this.armature.sneak ? "yes" : "no"
        );
    }

    private void setupOptionButton(String option, Consumer<ButtonComponent> updateValue, Supplier<String> valueKey) {
        var button = this.uiAdapter.rootComponent
            .childById(FlowLayout.class, option + "-controls")
            .childById(ButtonComponent.class, "button");

        button.setMessage(Text.translatable(this.armature.getCachedState().getBlock().getTranslationKey() + "." + option + "." + valueKey.get()));
        button.onPress(updateValue.andThen(buttonComponent -> {
            button.setMessage(Text.translatable(this.armature.getCachedState().getBlock().getTranslationKey() + "." + option + "." + valueKey.get()));
            this.sendProperties();
        }));
    }

    @Override
    public void tick() {
        super.tick();
        this.meshes.mainMesh.scheduleRebuild();
        this.meshes.peripheralMesh.scheduleRebuild();
    }

    private void sendProperties() {
        AffinityNetwork.CHANNEL.clientHandle().send(new VillagerArmatureBlockEntity.SetPropertiesPacket(this.armature));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();

        this.meshes.mainMesh.reset();
        this.meshes.peripheralMesh.reset();
    }

    private class MeshesComponent extends BaseComponent {

        private final BlockRenderView mainView;
        private final BlockRenderView peripheralView;

        private final WorldMesh mainMesh;
        private final WorldMesh peripheralMesh;

        private final Interpolator rotation;
        private final Interpolator slant = new Interpolator(30);
        private final Interpolator scale = new Interpolator(2).minValue(1.25).maxValue(7.5);

        private final double xSize, ySize, zSize;

        private @Nullable Vec2f lastHoverPosition = null;
        private long lastClickTime = 0;

        public MeshesComponent(VillagerArmatureBlockEntity armature) {
            var facing = armature.getCachedState().get(VillagerArmatureBlock.FACING);
            var mainPositions = Streams.stream(
                Iterables.transform(BlockPos.iterate(armature.getPos().offset(facing), armature.getPos().offset(facing, 5)), BlockPos::toImmutable)
            ).toList();

            var minPos = armature.getPos().offset(facing.rotateYCounterclockwise()).down();
            var maxPos = armature.getPos().offset(facing.rotateYClockwise()).offset(facing, 5);

            this.mainView = new FluxNetworkVisualizerScreen.RenderView(mainPositions);
            this.peripheralView = new FluxNetworkVisualizerScreen.RenderView(
                Streams.stream(
                    Iterables.transform(BlockPos.iterate(minPos, maxPos), BlockPos::toImmutable)
                ).filter(Predicate.not(mainPositions::contains)).toList()
            );

            this.mainMesh = new WorldMesh.Builder(this.mainView, mainPositions.getFirst(), mainPositions.getLast()).build();
            this.peripheralMesh = new WorldMesh.Builder(this.peripheralView, minPos, maxPos).build();

            this.rotation = new Interpolator(facing.asRotation() + 135);

            this.xSize = this.peripheralMesh.dimensions().getLengthX();
            this.ySize = this.peripheralMesh.dimensions().getLengthY();
            this.zSize = this.peripheralMesh.dimensions().getLengthZ();
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            this.lastHoverPosition = null;
            var client = VillagerArmatureScreen.this.client;
            var armature = VillagerArmatureScreen.this.armature;

            if (this.mainMesh.canRender() && this.peripheralMesh.canRender()) {

                // Begin model view / projection crimes

                float aspectRatio = client.getWindow().getFramebufferWidth() / (float) client.getWindow().getFramebufferHeight();

                RenderSystem.backupProjectionMatrix();
                RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-aspectRatio, aspectRatio, -1, 1, -1000, 3000), VertexSorter.BY_Z);

                var modelViewStack = RenderSystem.getModelViewStack();
                modelViewStack.pushMatrix();
                modelViewStack.identity();
                RenderSystem.applyModelViewMatrix();

                float scale = (this.scale.get() / 10f);

                // -- view matrix setup --

                var matrices = context.getMatrices();
                matrices.push();

                matrices.loadIdentity();
                matrices.translate((-1 + (this.x + this.width / 2f) / (float) VillagerArmatureScreen.this.width * 2) * aspectRatio, 1 - (this.y + this.height / 2f) / (float) VillagerArmatureScreen.this.height * 2, 0);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.slant.get()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotation.get()));
                matrices.translate(-this.xSize / 2f, -this.ySize / 2f, -this.zSize / 2f);

                // -- camera setup --

                var viewMatrix = matrices.peek().getPositionMatrix();

                var invProj = new Matrix4f(RenderSystem.getProjectionMatrix()).invert();
                var invView = new Matrix4f(viewMatrix).invert();

                var near = new Vector4f(0, 0, -1, 1).mul(invProj).mul(invView);
                var camera = MinecraftClient.getInstance().gameRenderer.getCamera();
                var prevCameraPos = camera.getPos();

                ((CameraInvoker) camera).affinity$etPos(new Vec3d(prevCameraPos.x + near.x, prevCameraPos.y + near.y, prevCameraPos.z + near.z));

                // -- lighting setup --

                final var lightDirection = new Vector4f(-.35f, .65f, 0f, 0);
                final var lightTransform = new Matrix4f(modelViewStack);
                lightTransform.invert();
                lightDirection.mul(lightTransform);

                final var transformedLightDirection = new Vector3f(lightDirection.x, lightDirection.y, lightDirection.z);
                RenderSystem.setShaderLights(transformedLightDirection, transformedLightDirection);

                //noinspection deprecation
                RenderSystem.runAsFancy(() -> {
                    var mainMeshOffset = this.mainMesh.startPos().subtract(this.peripheralMesh.startPos());

                    // -- main mesh --

                    matrices.push();
                    matrices.translate(mainMeshOffset.getX(), mainMeshOffset.getY(), mainMeshOffset.getZ());
                    this.renderMeshAndBlockEntities(matrices, this.mainMesh);
                    this.mainMesh.render(matrices);

                    var raycastResult = WorldMeshUtil.pickRay(
                        this.mainMesh,
                        this.mainView,
                        RenderSystem.getProjectionMatrix(),
                        matrices.peek().getPositionMatrix(),
                        mouseX, mouseY,
                        () -> this.scale.get() >= .75
                    );

                    // -- click target markers --

                    matrices.translate(-this.mainMesh.startPos().getX(), -this.mainMesh.startPos().getY(), -this.mainMesh.startPos().getZ());

                    var facing = armature.facing();
                    if (raycastResult.getType() != HitResult.Type.MISS && raycastResult instanceof BlockHitResult blockHit) {
                        this.renderBlockOutline(this.mainView, blockHit, matrices);

                        if (blockHit.getSide() == facing.getOpposite()) {
                            this.lastHoverPosition = new Vec2f((float)
                                switch (facing) {
                                    case NORTH -> 1 - MathHelper.fractionalPart(raycastResult.getPos().x);
                                    case SOUTH -> MathHelper.fractionalPart(raycastResult.getPos().x);
                                    case EAST -> MathHelper.fractionalPart(raycastResult.getPos().z);
                                    default -> 1 - MathHelper.fractionalPart(raycastResult.getPos().z);
                                },
                                (float) MathHelper.fractionalPart(raycastResult.getPos().y)
                            );

                            this.renderTargetMarker(matrices, raycastResult.getPos(), CROSSHAIR_MODEL_ID);
                        }
                    }

                    var rayOrigin = armature.raycastOrigin();
                    var currentTargetResult = this.mainView.raycast(new RaycastContext(
                        rayOrigin,
                        rayOrigin.add(Vec3d.of(facing.getVector()).multiply(5)),
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE,
                        client.player
                    ));

                    this.renderTargetMarker(matrices, currentTargetResult.getPos(), CROSSHAIR_PREVIEW_MODEL_ID);
                    matrices.pop();

                    client.getBufferBuilders().getEntityVertexConsumers().draw();

                    // -- peripheral mesh --

                    VISUALIZER_BUFFER.beginWrite(true, GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
                    this.renderMeshAndBlockEntities(matrices, this.peripheralMesh);
                    client.getBufferBuilders().getEntityVertexConsumers().draw();
                    VISUALIZER_BUFFER.endWrite();
                });

                matrices.pop();

                modelViewStack.popMatrix();
                RenderSystem.applyModelViewMatrix();

                RenderSystem.restoreProjectionMatrix();

                VISUALIZER_BUFFER.draw(new Color(1f, 1f, 1f, .8f));

                ((CameraInvoker) camera).affinity$etPos(prevCameraPos);

                // End model view / projection crimes
            }

            Interpolator.update(delta * .75f, this.scale, this.rotation, this.slant);
        }

        private void renderBlockOutline(BlockView world, BlockHitResult blockHit, MatrixStack matrices) {
            var outlineConsumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(CuboidRenderer.OUTLINE_LAYER);

            matrices.push();
            matrices.translate(blockHit.getBlockPos().getX(), blockHit.getBlockPos().getY(), blockHit.getBlockPos().getZ());
            var shape = world.getBlockState(blockHit.getBlockPos()).getOutlineShape(world, blockHit.getBlockPos());
            shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
                CuboidRenderer.line(matrices, outlineConsumer, (float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, Color.ofRgb(0xFFDC7F), .01f);
            });
            matrices.pop();
        }

        private void renderMeshAndBlockEntities(MatrixStack matrices, WorldMesh mesh) {
            var client = VillagerArmatureScreen.this.client;

            mesh.render(matrices);

            MixinHooks.forceBlockEntityRendering = true;
            mesh.renderInfo().blockEntities().forEach((blockPos, entity) -> {
                matrices.push();
                matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                client.getBlockEntityRenderDispatcher().render(entity, client.getRenderTickCounter().getTickDelta(false), matrices, client.getBufferBuilders().getEntityVertexConsumers());
                matrices.pop();
            });
            MixinHooks.forceBlockEntityRendering = false;
        }

        // -- epic methods stolen from ItemRenderer to draw the crosshair models --

        private void renderTargetMarker(MatrixStack matrices, Vec3d pos, Identifier modelId) {
            var client = VillagerArmatureScreen.this.client;
            var consumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getSolid());

            matrices.push();
            matrices.translate(pos.x - 0.09375, pos.y - 0.09375, pos.z - 0.09375);
            this.renderBakedModel(client.getBakedModelManager().getModel(modelId), matrices, consumer);
            matrices.pop();
        }

        private void renderBakedModel(BakedModel model, MatrixStack matrices, VertexConsumer vertices) {
            var random = Random.create();

            for (var direction : Direction.values()) {
                random.setSeed(42L);
                for (var quad : model.getQuads(null, direction, random)) {
                    vertices.quad(matrices.peek(), quad, 1f, 1f, 1f, 1f, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
                }
            }

            random.setSeed(42L);
            for (var quad : model.getQuads(null, null, random)) {
                vertices.quad(matrices.peek(), quad, 1f, 1f, 1f, 1f, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
            }
        }

        @Override
        public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
            var eventResult = super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);

            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.rotation.targetAdd(deltaX * 2);
                this.slant.targetAdd(deltaY * 2);
                return true;
            }

            return eventResult;
        }

        @Override
        public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
            this.scale.targetAdd(amount * .15 * this.scale.get());

            super.onMouseScroll(mouseX, mouseY, amount);
            return true;
        }

        @Override
        public boolean onMouseDown(double mouseX, double mouseY, int button) {
            var eventResult = super.onMouseDown(mouseX, mouseY, button);

            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (System.currentTimeMillis() - this.lastClickTime < 250) {
                    Interpolator.reset(this.scale, this.rotation, this.slant);
                }

                this.lastClickTime = System.currentTimeMillis();
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (this.lastHoverPosition != null) {
                    VillagerArmatureScreen.this.armature.clickPosition = this.lastHoverPosition;
                    VillagerArmatureScreen.this.sendProperties();
                }

                return true;
            }

            return eventResult;
        }

        @Override
        public boolean canFocus(FocusSource source) {
            return source == FocusSource.MOUSE_CLICK;
        }
    }
}
