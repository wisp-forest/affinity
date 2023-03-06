package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.MangroveBasketBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class MangroveBasketBlockEntityRenderer implements BlockEntityRenderer<MangroveBasketBlockEntity> {
    public MangroveBasketBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(MangroveBasketBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.containedState() == null) return;

        renderContents(entity.containedState(), entity.containedBlockEntity(), tickDelta, matrices, vertexConsumers, light, overlay);
    }

    public static void renderContents(BlockState containedState, BlockEntity containedBlockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var client = MinecraftClient.getInstance();

        matrices.push();
        matrices.translate(0.0625, 0.0625, 0.0625);
        matrices.scale(14.f / 16, 14.f / 16, 14.f / 16);

        if (containedState.getRenderType() == BlockRenderType.MODEL)
            client.getBlockRenderManager().renderBlockAsEntity(containedState, matrices, vertexConsumers, light, overlay);

        client.getBlockEntityRenderDispatcher().render(containedBlockEntity, tickDelta, matrices, vertexConsumers);

        matrices.pop();
    }
}
