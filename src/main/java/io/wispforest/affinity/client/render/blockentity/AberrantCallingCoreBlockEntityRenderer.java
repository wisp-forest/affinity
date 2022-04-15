package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.AberrantCallingCoreBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class AberrantCallingCoreBlockEntityRenderer implements BlockEntityRenderer<AberrantCallingCoreBlockEntity>, RotatingItemRenderer {

    public AberrantCallingCoreBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AberrantCallingCoreBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        final var stack = entity.getItem();
        if (stack.isEmpty()) return;

        this.renderItem(matrices, vertexConsumers, stack, 3000, .7f, .5f, .8f, .5f, light, overlay);
    }
}
