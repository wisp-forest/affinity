package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.GravitonTransducerBlockEntity;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class GravitonTransducerBlockEntityRenderer extends AffinityBlockEntityRenderer<GravitonTransducerBlockEntity> {

    public static final ModelPart SHARD;

    static {
        var floatingShardData = new ModelData();
        floatingShardData.getRoot().addChild(
                "shard",
                ModelPartBuilder.create()
                        .cuboid(0, 0, 0, 2, 4, 2)
                        .uv(0, 0),
                ModelTransform.NONE
        );

        SHARD = TexturedModelData.of(floatingShardData, 16, 16).createModel();
    }

    public GravitonTransducerBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected void render(GravitonTransducerBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var shardTier = AttunedShardTier.forItem(entity.shard().getItem());
        if (shardTier == AttunedShardTiers.NONE) return;

        matrices.push();
        matrices.translate(.5, .5 + Math.sin(time / 1000f) * .1f, .5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(time / 10f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time / 15f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(time / 20f));
        matrices.translate(-1 / 16f, -2 / 16f, -1 / 16f);

        var consumer = shardTier.sprite().getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());
        SHARD.render(matrices, consumer, light, overlay);

        matrices.pop();
    }
}
