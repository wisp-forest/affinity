package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class StaffPedestalBlockEntityRenderer extends AffinityBlockEntityRenderer<StaffPedestalBlockEntity> {

    private final FloatingItemRenderer itemRenderer;

    public StaffPedestalBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);

        this.itemRenderer = new FloatingItemRenderer(ctx.getItemRenderer());
        this.itemRenderer.scale = .75f;
        this.itemRenderer.x = .5f;
        this.itemRenderer.y = 1.15f;
        this.itemRenderer.z = .5f;
        this.itemRenderer.zRotation = 45f;
        this.itemRenderer.bobScale = .05f;
    }

    @Override
    protected void render(StaffPedestalBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        if (entity.facing() == Direction.DOWN) {
            matrices.translate(.5, .5, .5);
            matrices.multiply(entity.facing().getRotationQuaternion());
            matrices.translate(-.5, -.5, -.5);
        }

        this.itemRenderer.renderFloatingItem(matrices, vertexConsumers, time, entity.getPos().asLong(), entity.getItem(), entity.getWorld(), light, overlay);
        matrices.pop();
    }
}
