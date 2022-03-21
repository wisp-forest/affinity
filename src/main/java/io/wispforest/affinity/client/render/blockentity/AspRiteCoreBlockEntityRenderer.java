package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.AspRiteCoreBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class AspRiteCoreBlockEntityRenderer implements BlockEntityRenderer<AspRiteCoreBlockEntity>, RotatingItemRenderer {

    public AspRiteCoreBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AspRiteCoreBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.renderItem(matrices, vertexConsumers, entity.getItem(), 5000, .65f, .5f, .75f, .5f, light, overlay);
    }
}
