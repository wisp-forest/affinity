package io.wispforest.affinity.client.screen;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.VillagerArmatureBlock;
import io.wispforest.affinity.blockentity.impl.VillagerArmatureBlockEntity;
import io.wispforest.affinity.client.screen.FluxNetworkVisualizerScreen.Interpolator;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.mixin.client.CameraInvoker;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.worldmesher.WorldMesh;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static io.wispforest.affinity.client.screen.FluxNetworkVisualizerScreen.VISUALIZER_BUFFER;

public class VillagerArmatureScreen extends BaseUIModelScreen<FlowLayout> {

    private final VillagerArmatureBlockEntity armature;

    private final BlockRenderView mainView;
    private final BlockRenderView peripheralView;

    private final WorldMesh mainMesh;
    private final WorldMesh peripheralMesh;

    private final Interpolator rotation = new Interpolator(45), slant = new Interpolator(30);
    private final Interpolator scale = new Interpolator(2);

    private final double xSize, ySize, zSize;

    private @Nullable Vec2f lastHoverPosition = null;

    public VillagerArmatureScreen(VillagerArmatureBlockEntity armature) {
        super(FlowLayout.class, Affinity.id("villager_armature"));
        this.armature = armature;

        var facing = armature.getCachedState().get(VillagerArmatureBlock.FACING);
        var mainPositions = StreamSupport.stream(
            Iterables.transform(BlockPos.iterate(armature.getPos().offset(facing), armature.getPos().offset(facing, 5)), BlockPos::toImmutable).spliterator(),
            false
        ).toList();

        var minPos = armature.getPos().offset(facing.rotateYCounterclockwise()).down();
        var maxPos = armature.getPos().offset(facing.rotateYClockwise()).offset(facing, 5);

        this.mainView = new FluxNetworkVisualizerScreen.RenderView(mainPositions);
        this.peripheralView = new FluxNetworkVisualizerScreen.RenderView(
            StreamSupport.stream(
                Iterables.transform(BlockPos.iterate(minPos, maxPos), BlockPos::toImmutable).spliterator(),
                false
            ).filter(Predicate.not(mainPositions::contains)).toList()
        );

        this.mainMesh = new WorldMesh.Builder(this.mainView, mainPositions.getFirst(), mainPositions.getLast()).build();
        this.peripheralMesh = new WorldMesh.Builder(this.peripheralView, minPos, maxPos).build();

        this.xSize = this.peripheralMesh.dimensions().getLengthX();
        this.ySize = this.peripheralMesh.dimensions().getLengthY();
        this.zSize = this.peripheralMesh.dimensions().getLengthZ();

        this.rotation.set(facing.asRotation() + 135);

    }

    @Override
    protected void build(FlowLayout rootComponent) {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);

        if (this.peripheralMesh.canRender()) {

            // Begin model view / projection crimes

            float aspectRatio = this.client.getWindow().getFramebufferWidth() / (float) this.client.getWindow().getFramebufferHeight();

            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-aspectRatio, aspectRatio, -1, 1, -1000, 3000), VertexSorter.BY_Z);

            var modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.identity();

            float ageScalar = /*Math.min(1, this.age / 20f)*/ 1f;
            float visualizerScale = .75f + Easing.EXPO.apply(ageScalar) * .25f;

            float scale = (this.scale.get() / 10f) * visualizerScale;
            modelViewStack.scale(scale, scale, scale);

//            modelViewStack.translate(this.xOffset.get() / 2600f, this.yOffset.get() / -2600f, 0);

            modelViewStack.rotate(RotationAxis.POSITIVE_X.rotationDegrees(this.slant.get()));
            modelViewStack.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotation.get()));

            RenderSystem.applyModelViewMatrix();

            var matrices = context.getMatrices();
            matrices.push();

            matrices.loadIdentity();
            matrices.translate(-this.xSize / 2f, -this.ySize / 2f, -this.zSize / 2f);

            var viewMatrix = new Matrix4f(modelViewStack).mul(matrices.peek().getPositionMatrix());

            var invProj = new Matrix4f(RenderSystem.getProjectionMatrix()).invert();
            var invView = new Matrix4f(viewMatrix).invert();

            var near = new Vector4f(0, 0, -1, 1).mul(invProj).mul(invView);
            var camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            var prevCameraPos = camera.getPos();

            ((CameraInvoker) camera).affinity$etPos(new Vec3d(prevCameraPos.x + near.x, prevCameraPos.y + near.y, prevCameraPos.z + near.z));

            //noinspection deprecation
            RenderSystem.runAsFancy(() -> {
                var offset = this.mainMesh.startPos().subtract(this.peripheralMesh.startPos());

                var meshViewStack = new MatrixStack();
                meshViewStack.peek().getPositionMatrix().set(modelViewStack);
                meshViewStack.translate((float) (-this.xSize / 2f) + offset.getX(), (float) (-this.ySize / 2f) + offset.getY(), (float) (-this.zSize / 2f) + offset.getZ());
                this.mainMesh.render(meshViewStack);

                MixinHooks.forceBlockEntityRendering = true;
                this.mainMesh.renderInfo().blockEntities().forEach((blockPos, entity) -> {
                    matrices.push();
                    matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    this.client.getBlockEntityRenderDispatcher().render(entity, 0, matrices, this.client.getBufferBuilders().getEntityVertexConsumers());
                    matrices.pop();
                });
                MixinHooks.forceBlockEntityRendering = false;

                var raycastResult = this.raycast(
                    RenderSystem.getProjectionMatrix(),
                    meshViewStack.peek().getPositionMatrix(),
                    mouseX, mouseY
                );

                var facing = this.armature.getCachedState().get(VillagerArmatureBlock.FACING);
                if (raycastResult.getType() != HitResult.Type.MISS && raycastResult instanceof BlockHitResult blockHit && blockHit.getSide() == facing.getOpposite()) {
                    matrices.push();
                    matrices.translate(-0.09375, -0.09375, -0.09375);
                    matrices.translate(-this.mainMesh.startPos().getX(), -this.mainMesh.startPos().getY(), -this.mainMesh.startPos().getZ());
                    matrices.translate(offset.getX(), offset.getY(), offset.getZ());
                    matrices.translate(raycastResult.getPos().x, raycastResult.getPos().y, raycastResult.getPos().z);

                    this.lastHoverPosition = facing.getAxis() == Direction.Axis.X
                        ? new Vec2f((float) (raycastResult.getPos().z % 1), (float) (raycastResult.getPos().y % 1))
                        : new Vec2f((float) (raycastResult.getPos().x % 1), (float) (raycastResult.getPos().y % 1));

                    var consumer = this.client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getSolid());
                    this.renderBakedItemModel(
                        this.client.getBakedModelManager().getModel(Affinity.id("villager_armature_crosshair")),
                        LightmapTextureManager.MAX_LIGHT_COORDINATE,
                        OverlayTexture.DEFAULT_UV, matrices, consumer
                    );
                    matrices.pop();
                } else {
                    this.lastHoverPosition = null;
                }

                this.client.getBufferBuilders().getEntityVertexConsumers().draw();

                VISUALIZER_BUFFER.beginWrite(true, GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

                meshViewStack.loadIdentity();
                meshViewStack.peek().getPositionMatrix().set(modelViewStack);
                meshViewStack.translate((float) (-this.xSize / 2f), (float) (-this.ySize / 2f), (float) (-this.zSize / 2f));
                this.peripheralMesh.render(meshViewStack);

                MixinHooks.forceBlockEntityRendering = true;
                this.peripheralMesh.renderInfo().blockEntities().forEach((blockPos, entity) -> {
                    matrices.push();
                    matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    this.client.getBlockEntityRenderDispatcher().render(entity, 0, matrices, this.client.getBufferBuilders().getEntityVertexConsumers());
                    matrices.pop();
                });
                MixinHooks.forceBlockEntityRendering = false;

                this.client.getBufferBuilders().getEntityVertexConsumers().draw();

                VISUALIZER_BUFFER.endWrite();
            });

            // Raycast while we still can,
            // before all transformations are reset
//            var raycastResult = this.raycast(
//                RenderSystem.getProjectionMatrix(),
//                modelViewStack.mul(matrices.peek().getPositionMatrix()),
//                mouseX, mouseY
//            );

            matrices.pop();

            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();

            RenderSystem.restoreProjectionMatrix();

            VISUALIZER_BUFFER.draw(new Color(1f, 1f, 1f, .25f));

            ((CameraInvoker) camera).affinity$etPos(prevCameraPos);

            // End model view / projection crimes
        }

        super.render(context, mouseX, mouseY, delta);

        Interpolator.update(delta * .75f, this.scale, this.rotation, this.slant);
    }

    private HitResult raycast(Matrix4f projection, Matrix4f viewMatrix, double mouseX, double mouseY) {
        // If the scale is too low, we don't bother raycasting for two reasons
        // - the ray attains ungodly length (in excess of 100k+ blocks)
        // - the user can't really precisely aim at anything anyways
        if (scale.get() < .75f) return BlockHitResult.createMissed(Vec3d.ZERO, Direction.NORTH, BlockPos.ORIGIN);

        var window = MinecraftClient.getInstance().getWindow();
        float x = (float) ((2f * window.getScaleFactor() * mouseX) / window.getFramebufferWidth() - 1f);
        float y = (float) (1f - (2f * window.getScaleFactor() * mouseY) / window.getFramebufferHeight());

        // Unproject and compute ray enter/exit positions

        var invProj = new Matrix4f(projection).invert();
        var invView = new Matrix4f(viewMatrix).invert();

        var near = new Vector4f(x, y, -1, 1).mul(invProj).mul(invView);
        var far = new Vector4f(x, y, 1, 1).mul(invProj).mul(invView);

        // Since the ray points we get are at bogus positions very, very far away due to the
        // orthographic projection we compute the ray and move the points in by 20% and 65% respectively
        // to eliminate about 85% of the problem space. This is a fine optimization to make since
        // no real network will (or even can) ever extend over 20k blocks
        //
        // This results in about a 2x performance gain (170 -> 350FPS on my machine)
        // glisco, 07.02.2023

        var ray = new Vector4f(far).sub(near);

        near.add(ray.mul(.2f));
        far.add(ray.mul(1f / .2f * -.65f));

        // Now that we have somewhat sane ray points, we just hand off to vanilla raycasting
        // in the masked block render view we created for meshing the network in the first place

        var origin = this.mainMesh.startPos();
        return this.mainView.raycast(new RaycastContext(
            new Vec3d(origin.getX() + near.x, origin.getY() + near.y, origin.getZ() + near.z),
            new Vec3d(origin.getX() + far.x, origin.getY() + far.y, origin.getZ() + far.z),
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            client.player
        ));
    }

    private void renderBakedItemModel(BakedModel model, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
        Random random = Random.create();

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            this.renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, random), light, overlay);
        }

        random.setSeed(42L);
        this.renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, random), light, overlay);
    }

    private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay) {
        for (var quad : quads) {
            vertices.quad(matrices.peek(), quad, 1f, 1f, 1f, 1f, light, overlay);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.rotation.targetAdd(deltaX * 2);
            this.slant.targetAdd(deltaY * 2);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scale.targetAdd(verticalAmount * .15 * this.scale.get());
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
//            if (System.currentTimeMillis() - this.lastClickTime < 250) {
//                Interpolator.reset(this.scale, this.rotation, this.slant, this.xOffset, this.yOffset);
//            }
//
//            this.lastClickTime = System.currentTimeMillis();

            if (this.lastHoverPosition != null) {
                AffinityNetwork.CHANNEL.clientHandle().send(new VillagerArmatureBlockEntity.SetClickPositionPacket(
                    this.armature.getPos(),
                    this.lastHoverPosition
                ));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();

        this.mainMesh.reset();
        this.peripheralMesh.reset();
    }
}
