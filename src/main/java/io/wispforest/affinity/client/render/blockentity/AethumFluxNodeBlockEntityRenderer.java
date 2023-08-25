package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.AethumFluxNodeBlockEntity;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class AethumFluxNodeBlockEntityRenderer extends AffinityBlockEntityRenderer<AethumFluxNodeBlockEntity> {

    public static final ModelPart FLOATING_SHARD;

    static {
        var floatingShardData = new ModelData();
        floatingShardData.getRoot().addChild(
                "floatingShard",
                ModelPartBuilder.create()
                        .cuboid(0, 0, 0, 2, 4, 2)
                        .uv(0, 0),
                ModelTransform.NONE
        );

        FLOATING_SHARD = TexturedModelData.of(floatingShardData, 16, 16).createModel();
    }

    public AethumFluxNodeBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected void render(AethumFluxNodeBlockEntity node, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        node.shardActivity += Delta.compute(node.shardActivity, node.validForTransfer() ? 1f : 0.15f, frameDelta * .5f);
        node.time += frameDelta * 50 * node.shardActivity;
        float angle = (float) ((node.time / -2000d) % (2 * Math.PI));

        // --------------
        // Link rendering
        // --------------

        var badColor = Color.ofRgb(0xE90064);
        int linkColor = badColor.interpolate(Affinity.AETHUM_FLUX_COLOR, node.shardActivity).argb();

        var nodeLinkPos = Vec3d.ofCenter(node.getPos()).add(node.linkAttachmentPointOffset());
        for (var link : node.linkedMembers()) {
            var member = Affinity.AETHUM_MEMBER.find(node.getWorld(), link, null);
            if (member == null) continue;

            LinkRenderer.addLink(nodeLinkPos, Vec3d.ofCenter(link).add(member.linkAttachmentPointOffset()), linkColor);
        }


        // -------------
        // Central shard
        // -------------

        matrices.push();

        double shardHeight = Math.sin(node.time / 1000d) * .015;
        matrices.translate(0.4375, node.shardHeight() + shardHeight, 0.4375);

        matrices.push();

        matrices.translate(.0625, 0, .0625);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-angle));
        matrices.translate(-.0625, 0, -.0625);

        var consumer = node.tier().sprite().getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());
        if (node.hasShard()) FLOATING_SHARD.render(matrices, consumer, light, overlay);

        matrices.pop();

        // ---------------
        // Floating shards
        // ---------------

        if (node.isUpgradeable()) {
            matrices.translate(0, -shardHeight, 0);
            consumer = AttunedShardTiers.CRUDE.sprite().getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());

            node.renderShardCount += Delta.compute(node.renderShardCount, node.outerShardCount(), frameDelta * .5f);

            for (int i = 0; i < node.outerShardCount(); i++) {
                matrices.push();

                var shardAngle = node.renderShardCount + (float) (angle + i * (2 / node.renderShardCount) * Math.PI);

                matrices.translate(.0625, 0, .0625);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(shardAngle));
                matrices.translate(.35 - .0625, Math.sin(shardAngle + angle) * .05, -.0625);

                FLOATING_SHARD.render(matrices, consumer, light, overlay);

                matrices.pop();
            }
        }

        matrices.pop();
    }

}
