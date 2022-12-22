package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class StaffPedestalBlockEntityRenderer implements BlockEntityRenderer<StaffPedestalBlockEntity>, RotatingItemRenderer {

    public StaffPedestalBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

    @Override
    public void render(StaffPedestalBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        final var stack = entity.getItem();
        if (stack.isEmpty()) return;

        this.renderItem(matrices, vertexConsumers, stack, 5000, .75f, .5f, 1.15f, .5f, 45f, .05f, light, overlay);
    }
}
