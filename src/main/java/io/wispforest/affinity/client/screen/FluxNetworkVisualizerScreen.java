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
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.worldmesher.WorldMesh;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
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
import org.lwjgl.opengl.GL11;

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

    private final WorldMesh mesh;
    private final double xSize, ySize, zSize;

    private int networkMembers = 0;
    private int networkNodes = 0;
    private int networkCapacity = 0;

    private int xOffset = 0, yOffset = 0;
    private int rotation = 45, slant = 30;

    private int scale;

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

        this.scale = (int) Math.min(1500 / (Math.max(xSize, Math.max(ySize, zSize))), 500);
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
            float aspectRatio = this.client.getWindow().getFramebufferWidth() / (float) this.client.getWindow().getFramebufferHeight();

            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-aspectRatio, aspectRatio, -1, 1, -1000, 3000));

            var modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.push();
            modelViewStack.loadIdentity();

            float scale = this.scale / 1000f;
            modelViewStack.scale(scale, scale, scale);

            modelViewStack.translate(this.xOffset / 2600d, this.yOffset / -2600d, 0);

            modelViewStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.slant));
            modelViewStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotation));

            RenderSystem.applyModelViewMatrix();

            matrices.push();

            matrices.loadIdentity();
            matrices.translate(-this.xSize / 2f, -this.ySize / 2f, -this.zSize / 2f);

            //noinspection deprecation
            RenderSystem.runAsFancy(() -> {
                mesh.getRenderInfo().getBlockEntities().forEach((blockPos, entity) -> {
                    matrices.push();
                    matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    client.getBlockEntityRenderDispatcher().render(entity, 0, matrices, this.client.getBufferBuilders().getEntityVertexConsumers());
                    matrices.pop();
                });

                this.client.getBufferBuilders().getEntityVertexConsumers().draw();

                modelViewStack.translate(-this.xSize / 2f, -this.ySize / 2f, -this.zSize / 2f);
                this.mesh.render(modelViewStack);
            });

            matrices.pop();

            modelViewStack.pop();
            RenderSystem.applyModelViewMatrix();

            RenderSystem.restoreProjectionMatrix();
        }

        GlStateManager._clear(GL11.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            double xScaling = (100d / this.scale) * (this.client.getWindow().getWidth() / (float) this.client.getWindow().getScaledWidth());
            double yScaling = (100d / this.scale) * (this.client.getWindow().getHeight() / (float) this.client.getWindow().getScaledHeight());

            this.xOffset += (int) (50 * deltaX * xScaling);
            this.yOffset += (int) (50 * deltaY * yScaling);
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.rotation += (int) (deltaX * 2);
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.slant += (int) (deltaY * 2);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.scale += (int) (amount * Math.max(1, this.scale * 0.075));
        return true;
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
