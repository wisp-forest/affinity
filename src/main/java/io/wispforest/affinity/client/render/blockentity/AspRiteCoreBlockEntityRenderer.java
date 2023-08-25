package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.AspRiteCoreBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class AspRiteCoreBlockEntityRenderer extends AffinityBlockEntityRenderer<AspRiteCoreBlockEntity> {

    private final FloatingItemRenderer itemRenderer;

    public AspRiteCoreBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);

        this.itemRenderer = new FloatingItemRenderer(ctx.getItemRenderer());
        this.itemRenderer.scale = .65f;
        this.itemRenderer.x = .5f;
        this.itemRenderer.y = .75f;
        this.itemRenderer.z = .5f;
    }

    @Override
    protected void render(AspRiteCoreBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.itemRenderer.renderFloatingItem(matrices, vertexConsumers, time, entity.getPos().asLong(), entity.getItem(), entity.getWorld(), light, overlay);
    }
}
