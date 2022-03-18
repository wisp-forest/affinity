package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.AethumFluxNodeBlockEntity;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import net.minecraft.client.model.*;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.LightType;

import java.util.Random;

public class AethumFluxNodeBlockEntityRenderer implements BlockEntityRenderer<AethumFluxNodeBlockEntity> {

    public static final ModelPart FLOATING_SHARD;
    private static final Random RANDOM = new Random();

    static {
        ModelData floatingShardData = new ModelData();
        floatingShardData.getRoot().addChild("floatingShard", ModelPartBuilder.create()
                .cuboid(0, 0, 0, 2, 4, 2)
                .uv(0, 0), ModelTransform.NONE);
        FLOATING_SHARD = TexturedModelData.of(floatingShardData, 16, 16).createModel();
    }

    public AethumFluxNodeBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AethumFluxNodeBlockEntity node, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        // --------------
        // Link rendering
        // --------------

        int startColor = node.hasShard() ? 0xff4FC1E9 : 0xffff0000;
        int endColor = node.hasShard() ? 0xff7A57D1 : 0xffff0000;

        for (var linkedMember : node.linkedMembers()) {
            var offset = Vec3d.ofCenter(linkedMember).subtract(Vec3d.of(node.getPos()));

            vertexConsumers.getBuffer(RenderLayer.LINES)
                    .vertex(matrices.peek().getPositionMatrix(), .5f, .5f, .5f)
                    .color(startColor)
                    .normal(1, 0, 1).next();
            vertexConsumers.getBuffer(RenderLayer.LINES)
                    .vertex(matrices.peek().getPositionMatrix(), (float) offset.x, (float) offset.y, (float) offset.z)
                    .color(endColor)
                    .normal(1, 0, 1).next();
        }

        // -------------------------
        // Prerequisite calculations
        // -------------------------

        var packedLight = LightmapTextureManager.pack(
                node.getWorld().getLightLevel(LightType.BLOCK, node.getPos()) - 2,
                node.getWorld().getLightLevel(LightType.SKY, node.getPos()));

        RANDOM.setSeed(node.getPos().asLong());
        long time = System.currentTimeMillis() + RANDOM.nextLong(5000);

        float angle = (float) ((time / -2000d) % (2 * Math.PI));

        // -------------
        // Central shard
        // -------------

        matrices.push();

        double shardHeight = Math.sin(time / 1000d) * .015;
        matrices.translate(0.4375, node.shardHeight() + shardHeight, 0.4375);

        matrices.push();

        matrices.translate(.0625, 0, .0625);
        matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(-angle));
        matrices.translate(-.0625, 0, -.0625);

        var consumer = node.tier().sprite().getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());
        if (node.hasShard()) FLOATING_SHARD.render(matrices, consumer, packedLight, overlay);

        matrices.pop();

        // ---------------
        // Floating shards
        // ---------------

        if (node.isUpgradeable()) {
            matrices.translate(0, -shardHeight, 0);
            consumer = AttunedShardTiers.CRUDE.sprite().getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());

            node.renderShardCount = MathUtil.proportionalApproach(node.renderShardCount, node.outerShardCount(), .0025f, .001f);

            for (int i = 0; i < node.outerShardCount(); i++) {
                matrices.push();

                var shardAngle = node.renderShardCount + (float) (angle + i * (2 / node.renderShardCount) * Math.PI);

                matrices.translate(.0625, 0, .0625);
                matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(shardAngle));
                matrices.translate(.35 - .0625, Math.sin(shardAngle + angle) * .05, -.0625);

                FLOATING_SHARD.render(matrices, consumer, packedLight, overlay);

                matrices.pop();
            }
        }

        matrices.pop();
    }

}
