package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.MangroveBasketBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class MangroveBasketBlockEntityRenderer implements BlockEntityRenderer<MangroveBasketBlockEntity> {

    private final BlockRenderManager renderManager;
    private final BlockEntityRenderDispatcher renderDispatcher;

    public MangroveBasketBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.renderManager = ctx.getRenderManager();
        this.renderDispatcher = ctx.getRenderDispatcher();
    }

    @Override
    public void render(MangroveBasketBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.containedState() == null) return;
        renderContents(
                this.renderManager,
                this.renderDispatcher,
                entity.containedState(),
                entity.containedBlockEntity(),
                tickDelta,
                matrices,
                vertexConsumers,
                light,
                overlay
        );
    }

    public static void renderContents(BlockRenderManager renderManager, BlockEntityRenderDispatcher renderDispatcher, BlockState containedState, BlockEntity containedBlockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(.0625, .0625, .0625);
        matrices.scale(14f / 16f, 14f / 16f, 14f / 16f);

        renderManager.renderBlockAsEntity(containedState, matrices, vertexConsumers, light, overlay);
        renderDispatcher.render(containedBlockEntity, tickDelta, matrices, vertexConsumers);

        matrices.pop();
    }
}
