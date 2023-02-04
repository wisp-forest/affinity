package io.wispforest.affinity.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.aethumflux.net.MultiblockAethumNetworkMember;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayDeque;
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

    private static Framebuffer networkFramebuffer = null;

    private final WorldMesh mesh;
    private final double xSize, ySize, zSize;

    private int networkMembers = 0;
    private int networkNodes = 0;
    private int networkCapacity = 0;

    private final Interpolator xOffset = new Interpolator(0), yOffset = new Interpolator(0);
    private final Interpolator rotation = new Interpolator(45), slant = new Interpolator(30);

    private final Interpolator scale;

    private float age = 0;
    private long lastClickTime = 0;

    public FluxNetworkVisualizerScreen(AethumNetworkMemberBlockEntity initialMember) {
        super(FlowLayout.class, DataSource.file("../src/main/resources/assets/affinity/owo_ui/flux_network_visualizer.xml"));

        var members = new HashSet<BlockPos>();

        var queue = new ArrayDeque<BlockPos>();
        queue.add(initialMember.getPos());

        while (!queue.isEmpty()) {
            var memberPos = queue.poll();

            var peer = Affinity.AETHUM_MEMBER.find(MinecraftClient.getInstance().world, memberPos, null);
            if (peer == null) continue;

            members.add(memberPos);
            if (peer instanceof MultiblockAethumNetworkMember multiblock) members.addAll(multiblock.memberBlocks());

            this.networkMembers++;
            this.networkCapacity += peer.fluxCapacity();
            if (peer instanceof AethumNetworkNode) this.networkNodes++;

            for (var neighbor : peer.linkedMembers()) {
                if (members.contains(neighbor) || queue.contains(neighbor)) continue;
                queue.add(neighbor);
            }
        }

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

        this.mesh = new WorldMesh.Builder(new RenderView(members), new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ)).build();

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
                networkFramebuffer().clear(MinecraftClient.IS_SYSTEM_MAC);
                networkFramebuffer.beginWrite(false);

                mesh.getRenderInfo().getBlockEntities().forEach((blockPos, entity) -> {
                    matrices.push();
                    matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    client.getBlockEntityRenderDispatcher().render(entity, 0, matrices, this.client.getBufferBuilders().getEntityVertexConsumers());
                    matrices.pop();
                });

                this.client.getBufferBuilders().getEntityVertexConsumers().draw();

                modelViewStack.translate(-this.xSize / 2f, -this.ySize / 2f, -this.zSize / 2f);
                this.mesh.render(modelViewStack);

                GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, prevFramebuffer);
            });

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

            RenderSystem.setShaderTexture(0, networkFramebuffer.getColorAttachment());
            Drawer.drawTexture(
                    matrices,
                    0, 0,
                    this.width, this.height,
                    0, networkFramebuffer.textureHeight,
                    networkFramebuffer.textureWidth, -networkFramebuffer.textureHeight,
                    networkFramebuffer.textureWidth, networkFramebuffer.textureHeight
            );

            RenderSystem.setShaderColor(1, 1, 1, 1);
            matrices.pop();
        }

        super.render(matrices, mouseX, mouseY, delta);

        this.age += delta;
        Interpolator.update(delta * .75f, this.scale, this.rotation, this.slant, this.xOffset, this.yOffset);
    }

    private Framebuffer networkFramebuffer() {
        if (networkFramebuffer == null) {
            networkFramebuffer = new SimpleFramebuffer(this.client.getFramebuffer().textureWidth, this.client.getFramebuffer().textureHeight, true, MinecraftClient.IS_SYSTEM_MAC);
            WindowResizeCallback.EVENT.register((client_, window) -> networkFramebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC));
        }

        return networkFramebuffer;
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

    private static class RenderView implements BlockRenderView {

        private final Set<BlockPos> passthroughPositions;
        private final ClientWorld world;

        private RenderView(Collection<BlockPos> passthroughPositions) {
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
