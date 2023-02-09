package io.wispforest.affinity.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.aethumflux.net.MultiblockAethumNetworkMember;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.client.render.CrosshairStatProvider;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.util.Delta;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.worldmesher.WorldMesh;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FluxNetworkVisualizerScreen extends BaseUIModelScreen<FlowLayout> {

    private static final Surface TOOLTIP_SURFACE = (matrices, component) -> {
        var buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        TooltipBackgroundRenderer.render(Drawer::fillGradient, matrices.peek().getPositionMatrix(), buffer, component.x() + 4, component.y() + 4, component.width() - 8, component.height() - 8, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator.getInstance().draw();
    };

    private static Framebuffer visualizerFramebuffer = null;

    private final WorldMesh mesh;
    private final BlockRenderView world;
    private final double xSize, ySize, zSize;

    private int networkMembers = 0;
    private int networkNodes = 0;
    private int networkCapacity = 0;

    private final Interpolator xOffset = new Interpolator(0), yOffset = new Interpolator(0);
    private final Interpolator rotation = new Interpolator(45), slant = new Interpolator(30);

    private final Interpolator scale;

    private float age = 0;
    private long lastClickTime = 0;

    private BlockEntity focusedEntity = null;
    private float focusViewTime = 0;

    public FluxNetworkVisualizerScreen(AethumNetworkMemberBlockEntity initialMember) {
        super(FlowLayout.class, DataSource.file("../src/main/resources/assets/affinity/owo_ui/flux_network_visualizer.xml"));

        var members = AethumNetworkMember.traverseNetwork(MinecraftClient.getInstance().world, initialMember.getPos(), (peer, isMultiblockChild) -> {
            if (!isMultiblockChild) {
                this.networkMembers++;
                this.networkCapacity += peer.fluxCapacity();
                if (peer instanceof AethumNetworkNode) this.networkNodes++;
            } else {
                this.networkCapacity += peer.fluxCapacity();
            }
        });

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (var member : members) {
            minX = Math.min(member.getX(), minX);
            minY = Math.min(member.getY(), minY);
            minZ = Math.min(member.getZ(), minZ);

            maxX = Math.max(member.getX(), maxX);
            maxY = Math.max(member.getY(), maxY);
            maxZ = Math.max(member.getZ(), maxZ);
        }

        this.world = new RenderView(members);
        this.mesh = new WorldMesh.Builder(this.world, new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ)).build();

        this.xSize = this.mesh.dimensions().getXLength() + 1;
        this.ySize = this.mesh.dimensions().getYLength() + 1;
        this.zSize = this.mesh.dimensions().getZLength() + 1;

        this.scale = new Interpolator(Math.min(15 / (Math.max(xSize, Math.max(ySize, zSize))), 5));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(FlowLayout.class, "stats-tooltip").surface(TOOLTIP_SURFACE);

        rootComponent.childById(LabelComponent.class, "member-count-label").text(
                Text.translatable("gui.affinity.flux_network_visualizer.member_count", this.networkMembers));
        rootComponent.childById(LabelComponent.class, "node-count-label").text(
                Text.translatable("gui.affinity.flux_network_visualizer.node_count", this.networkNodes));
        rootComponent.childById(LabelComponent.class, "flux-capacity-label").text(
                Text.translatable("gui.affinity.flux_network_visualizer.flux_capacity", this.networkCapacity));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (this.mesh.canRender()) {

            // Begin model view / projection crimes

            float aspectRatio = this.client.getWindow().getFramebufferWidth() / (float) this.client.getWindow().getFramebufferHeight();

            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-aspectRatio, aspectRatio, -1, 1, -1000, 3000));

            var modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.push();
            modelViewStack.loadIdentity();

            float scale = this.scale.get() / 10f;
            modelViewStack.scale(scale, scale, scale);

            modelViewStack.translate(this.xOffset.get() / 2600d, this.yOffset.get() / -2600d, 0);

            modelViewStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.slant.get()));
            modelViewStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotation.get()));

            RenderSystem.applyModelViewMatrix();

            matrices.push();

            matrices.loadIdentity();
            matrices.translate(-this.xSize / 2f, -this.ySize / 2f, -this.zSize / 2f);

            //noinspection deprecation
            RenderSystem.runAsFancy(() -> {
                int prevFramebuffer = GlStateManager.getBoundFramebuffer();
                visualizerFramebuffer().clear(MinecraftClient.IS_SYSTEM_MAC);
                visualizerFramebuffer.beginWrite(false);

                mesh.getRenderInfo().getBlockEntities().forEach((blockPos, entity) -> {
                    matrices.push();
                    matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    this.client.getBlockEntityRenderDispatcher().render(entity, 0, matrices, this.client.getBufferBuilders().getEntityVertexConsumers());
                    matrices.pop();
                });

                this.client.getBufferBuilders().getEntityVertexConsumers().draw();

                modelViewStack.translate(-this.xSize / 2f, -this.ySize / 2f, -this.zSize / 2f);
                this.mesh.render(modelViewStack);
                modelViewStack.translate(this.xSize / 2f, this.ySize / 2f, this.zSize / 2f);

                GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, prevFramebuffer);
            });

            // Raycast while we still can,
            // before all transformations are reset
            var raycastResult = this.raycast(
                    RenderSystem.getProjectionMatrix(),
                    modelViewStack.peek().getPositionMatrix().mul(matrices.peek().getPositionMatrix()),
                    mouseX, mouseY
            );

            matrices.pop();

            modelViewStack.pop();
            RenderSystem.applyModelViewMatrix();

            RenderSystem.restoreProjectionMatrix();

            // End model view / projection crimes

            float ageScalar = Math.min(1, this.age / 20f);
            float visualizerScale = .75f + Easing.EXPO.apply(ageScalar) * .25f;

            matrices.push();
            RenderSystem.setShaderColor(1, 1, 1, Easing.SINE.apply(ageScalar));

            matrices.translate(this.width / 2d, this.height / 2d, 0);
            matrices.scale(visualizerScale, visualizerScale, visualizerScale);
            matrices.translate(this.width / -2d, this.height / -2d, 0);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderSystem.setShaderTexture(0, visualizerFramebuffer.getColorAttachment());
            Drawer.drawTexture(
                    matrices,
                    0, 0,
                    this.width, this.height,
                    0, visualizerFramebuffer.textureHeight,
                    visualizerFramebuffer.textureWidth, -visualizerFramebuffer.textureHeight,
                    visualizerFramebuffer.textureWidth, visualizerFramebuffer.textureHeight
            );

            RenderSystem.setShaderColor(1, 1, 1, 1);
            matrices.pop();

            if (raycastResult instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
                var blockEntity = this.world.getBlockEntity(blockHit.getBlockPos());

                if (focusedEntity != blockEntity) {
                    // this conditional makes me uncomfortable
                    if (focusedEntity == null || blockEntity == null || (focusedEntity instanceof MultiblockAethumNetworkMember multiblock && !multiblock.memberBlocks().contains(blockEntity.getPos()))) {
                        this.focusedEntity = blockEntity;
                        this.focusViewTime = 0;
                    }
                }

                if (blockEntity instanceof CrosshairStatProvider statProvider) {
                    var entries = new ArrayList<CrosshairStatProvider.Entry>();
                    statProvider.appendTooltipEntries(entries);

                    entries.add(0, CrosshairStatProvider.Entry.text(Text.empty(), blockEntity.getCachedState().getBlock().getName()));

                    for (int i = 0; i < entries.size(); i++) {
                        var entry = entries.get(i);
                        float progress = MathHelper.clamp((this.focusViewTime - i * 2) / 10, 0, 1);

                        int yOffset = i * 10;
                        if (i > 0) yOffset += 3;

                        if (entry instanceof CrosshairStatProvider.TextEntry textEntry) {
                            client.textRenderer.draw(matrices, textEntry.icon(), mouseX + 10 + 1, mouseY + yOffset, (Math.max(4, (int) (0xFF * progress)) << 24) | 0xFFFFFF);
                        } else if (entry instanceof CrosshairStatProvider.TextAndIconEntry iconEntry) {
                            RenderSystem.enableBlend();
                            RenderSystem.setShaderColor(1, 1, 1, progress);
                            RenderSystem.setShaderTexture(0, iconEntry.texture());
                            RenderSystem.enableDepthTest();
                            Drawer.drawTexture(matrices, mouseX + 10, mouseY + yOffset, iconEntry.u(), iconEntry.v(), 8, 8, 32, 32);
                        }

                        client.textRenderer.drawWithShadow(matrices, entry.label(), mouseX + 10 + 15, mouseY + yOffset, (Math.max(4, (int) (0xFF * progress)) << 24) | 0xFFFFFF);
                    }
                }

                this.focusViewTime += delta;
            } else {
                this.focusedEntity = null;
            }
        }

        super.render(matrices, mouseX, mouseY, delta);

        this.age += delta;
        Interpolator.update(delta * .75f, this.scale, this.rotation, this.slant, this.xOffset, this.yOffset);
    }

    private Framebuffer visualizerFramebuffer() {
        if (visualizerFramebuffer == null) {
            visualizerFramebuffer = new SimpleFramebuffer(this.client.getFramebuffer().textureWidth, this.client.getFramebuffer().textureHeight, true, MinecraftClient.IS_SYSTEM_MAC);
            WindowResizeCallback.EVENT.register((client_, window) -> visualizerFramebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC));
        }

        return visualizerFramebuffer;
    }

    private HitResult raycast(Matrix4f projection, Matrix4f viewMatrix, double mouseX, double mouseY) {
        // If the scale is too low, we don't bother raycasting for two reasons
        // - the ray attains ungodly length (in excess of 100k+ blocks)
        // - the user can't really precisely aim at anything anyways
        if (scale.value < .75f) return BlockHitResult.createMissed(Vec3d.ZERO, Direction.NORTH, BlockPos.ORIGIN);

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

        var origin = this.mesh.startPos();
        return this.world.raycast(new RaycastContext(
                new Vec3d(origin.getX() + near.x, origin.getY() + near.y, origin.getZ() + near.z),
                new Vec3d(origin.getX() + far.x, origin.getY() + far.y, origin.getZ() + far.z),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                client.player
        ));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            double xScaling = (1d / this.scale.get()) * (this.client.getWindow().getWidth() / (float) this.client.getWindow().getScaledWidth());
            double yScaling = (1d / this.scale.get()) * (this.client.getWindow().getHeight() / (float) this.client.getWindow().getScaledHeight());

            this.xOffset.targetAdd(50 * deltaX * xScaling);
            this.yOffset.targetAdd(50 * deltaY * yScaling);
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.rotation.targetAdd(deltaX * 2);
            this.slant.targetAdd(deltaY * 2);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.scale.targetAdd(amount * .15 * this.scale.get());
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (System.currentTimeMillis() - this.lastClickTime < 250) {
                Interpolator.reset(this.scale, this.rotation, this.slant, this.xOffset, this.yOffset);
            }

            this.lastClickTime = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class Interpolator {

        private final float defaultValue;

        private float value;
        private float target;

        public Interpolator(double value) {
            this.defaultValue = this.target = this.value = (float) value;
        }

        public static void update(float delta, Interpolator... interpolators) {
            for (var interpolator : interpolators) {
                interpolator.value += Delta.compute(interpolator.value, interpolator.target, delta);
            }
        }

        public static void reset(Interpolator... interpolators) {
            for (var interpolator : interpolators) {
                interpolator.target = interpolator.defaultValue;
            }
        }

        public void targetAdd(double value) {
            this.target += value;
        }

        public void set(double value) {
            this.target = this.value = (float) value;
        }

        public float get() {
            return this.value;
        }
    }

    public static class RenderView implements BlockRenderView {

        private final Set<BlockPos> passthroughPositions;
        private final ClientWorld world;

        public RenderView(Collection<BlockPos> passthroughPositions) {
            this.passthroughPositions = new HashSet<>(passthroughPositions);
            this.world = MinecraftClient.getInstance().world;
        }

        @Override
        public float getBrightness(Direction direction, boolean shaded) {
            return this.world.getBrightness(direction, shaded);
        }

        @Override
        public LightingProvider getLightingProvider() {
            return this.world.getLightingProvider();
        }

        @Override
        public int getColor(BlockPos pos, ColorResolver colorResolver) {
            return this.world.getColor(pos, colorResolver);
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return this.passthroughPositions.contains(pos)
                    ? this.world.getBlockEntity(pos)
                    : null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return this.passthroughPositions.contains(pos)
                    ? this.world.getBlockState(pos)
                    : Blocks.AIR.getDefaultState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return this.passthroughPositions.contains(pos)
                    ? this.world.getFluidState(pos)
                    : Fluids.EMPTY.getDefaultState();
        }

        @Override
        public int getHeight() {
            return this.world.getHeight();
        }

        @Override
        public int getBottomY() {
            return this.world.getBottomY();
        }
    }
}
