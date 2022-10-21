package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.MangroveBasketBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class MangroveBasketBlockEntityRenderer implements BlockEntityRenderer<MangroveBasketBlockEntity> {
    private final BlockEntityRendererFactory.Context ctx;

    public MangroveBasketBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(MangroveBasketBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getContainedState() == null) return;

        matrices.push();
        matrices.translate(0.0625, 0.0625, 0.0625);
        matrices.scale(14.f / 16, 14.f / 16, 14.f / 16);

        ctx.getRenderManager().renderBlockAsEntity(entity.getContainedState(), matrices, vertexConsumers, light, overlay);
        ctx.getRenderDispatcher().render(entity.getContainedBlockEntity(), tickDelta, matrices, vertexConsumers);

        matrices.pop();
    }
}
