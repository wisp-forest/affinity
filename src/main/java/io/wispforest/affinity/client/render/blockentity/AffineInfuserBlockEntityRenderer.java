package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.AffineInfuserBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

public class AffineInfuserBlockEntityRenderer implements BlockEntityRenderer<AffineInfuserBlockEntity>, BuiltinItemRendererRegistry.DynamicItemRenderer {

    public static final ModelPart FLOATING_SHARD;

    static {
        ModelData floatingShardData = new ModelData();
        floatingShardData.getRoot().addChild("floatingShard", ModelPartBuilder.create()
                .cuboid(0, 0, 0, 2, 4, 2)
                .uv(0, 0), ModelTransform.NONE);
        FLOATING_SHARD = TexturedModelData.of(floatingShardData, 16, 16).createModel();
    }

    public AffineInfuserBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AffineInfuserBlockEntity node, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.renderShards(matrices, vertexConsumers, light, overlay);
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(AffinityBlocks.AFFINE_INFUSER.getDefaultState(), matrices, vertexConsumers, light, overlay);
        this.renderShards(matrices, vertexConsumers, light, overlay);
    }

    private void renderShards(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        float angle = (float) ((System.currentTimeMillis() / -2000d) % (2 * Math.PI));
        var consumer = AttunedShardTiers.CRUDE.sprite().getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());

        matrices.push();
        matrices.translate(.4375, .4375, .4375);

        for (int i = 0; i < 5; i++) {
            matrices.push();

            float shardAngle = angle - (float) (i * (Math.PI / 2.5f));

            matrices.translate(.0625, 0, .0625);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(shardAngle));
            matrices.translate(.35 - .0625, Math.sin(shardAngle + angle) * .15, -.0625);

            FLOATING_SHARD.render(matrices, consumer, light, overlay);

            matrices.pop();
        }

        matrices.pop();
    }
}
