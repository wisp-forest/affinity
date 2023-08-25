package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.RitualSocleBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class RitualSocleBlockEntityRenderer extends AffinityBlockEntityRenderer<RitualSocleBlockEntity> {

    private final FloatingItemRenderer itemRenderer;

    public RitualSocleBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);

        this.itemRenderer = new FloatingItemRenderer(ctx.getItemRenderer());
        this.itemRenderer.animationSpeed = 1.5f;
        this.itemRenderer.scale = .5f;
        this.itemRenderer.x = .5f;
        this.itemRenderer.y = .935f;
        this.itemRenderer.z = .5f;
    }

    @Override
    protected void render(RitualSocleBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.itemRenderer.renderFloatingItem(matrices, vertexConsumers, time, entity.getPos().asLong(), entity.getItem(), entity.getWorld(), light, overlay);
    }
}
