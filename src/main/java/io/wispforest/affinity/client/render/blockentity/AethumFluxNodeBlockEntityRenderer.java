package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.AethumFluxNodeBlockEntity;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class AethumFluxNodeBlockEntityRenderer implements BlockEntityRenderer<AethumFluxNodeBlockEntity> {

    private static final Matrix4f BILLBOARD_MATRIX = new Matrix4f();
    private static final Identifier LINK_TEXTURE_ID = Affinity.id("textures/aethum_link.png");

    public static boolean enableLinkRendering = true;

    public static final ModelPart FLOATING_SHARD;

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
        node.shardActivity += Delta.compute(node.shardActivity, node.validForTransfer() ? 1 : 0.15, MinecraftClient.getInstance().getLastFrameDuration() * .1);

        // --------------
        // Link rendering
        // --------------

        if (enableLinkRendering) {
            var badColor = Color.ofRgb(0xE90064);
            int color = badColor.interpolate(Affinity.AETHUM_FLUX_COLOR, node.shardActivity).argb();

            var cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
            var linkBuffer = vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(LINK_TEXTURE_ID, true));

            float nodeX = node.getPos().getX() + (float) node.linkAttachmentPointOffset().x;
            float nodeY = node.getPos().getY() + (float) node.linkAttachmentPointOffset().y;
            float nodeZ = node.getPos().getZ() + (float) node.linkAttachmentPointOffset().z;

            for (var linkedMember : node.linkedMembers()) {
                var member = Affinity.AETHUM_MEMBER.find(node.getWorld(), linkedMember, null);

                matrices.push();
                matrices.multiplyPositionMatrix(BILLBOARD_MATRIX.billboardCylindrical(
                        new Vector3f(
                                .5f + (float) node.linkAttachmentPointOffset().x,
                                .5f + (float) node.linkAttachmentPointOffset().y,
                                .5f + (float) node.linkAttachmentPointOffset().z),
                        new Vector3f(nodeX + .5f - cameraPos.x, nodeY + .5f - cameraPos.y, nodeZ + .5f - cameraPos.z),
                        new Vector3f(
                                linkedMember.getX() + (float) member.linkAttachmentPointOffset().x - nodeX,
                                linkedMember.getY() + (float) member.linkAttachmentPointOffset().y - nodeY,
                                linkedMember.getZ() + (float) member.linkAttachmentPointOffset().z - nodeZ)
                ));

                linkBuffer.vertex(matrices.peek().getPositionMatrix(), -.035f, 0, 0)
                        .color(color).texture(0, 0).light(light)
                        .normal(matrices.peek().getNormalMatrix(), 0, 1, 0)
                        .next();

                linkBuffer.vertex(matrices.peek().getPositionMatrix(), -.035f, 1, 0)
                        .color(color).texture(1, 0).light(light)
                        .normal(matrices.peek().getNormalMatrix(), 0, 1, 0)
                        .next();

                linkBuffer.vertex(matrices.peek().getPositionMatrix(), .035f, 1, 0)
                        .color(color).texture(1, 1).light(light)
                        .normal(matrices.peek().getNormalMatrix(), 0, 1, 0)
                        .next();

                linkBuffer.vertex(matrices.peek().getPositionMatrix(), .035f, 0, 0)
                        .color(color).texture(0, 1).light(light)
                        .normal(matrices.peek().getNormalMatrix(), 0, 1, 0)
                        .next();

                matrices.pop();
            }
        }

        // -------------------------
        // Prerequisite calculations
        // -------------------------

        var packedLight = LightmapTextureManager.pack(
                node.getWorld().getLightLevel(LightType.BLOCK, node.getPos()) - 2,
                node.getWorld().getLightLevel(LightType.SKY, node.getPos()));

        double time = node.time += MinecraftClient.getInstance().getLastFrameDuration() * 50 * node.shardActivity;
        float angle = (float) ((time / -2000d) % (2 * Math.PI));

        // -------------
        // Central shard
        // -------------

        matrices.push();

        double shardHeight = Math.sin(time / 1000d) * .015;
        matrices.translate(0.4375, node.shardHeight() + shardHeight, 0.4375);

        matrices.push();

        matrices.translate(.0625, 0, .0625);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-angle));
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
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(shardAngle));
                matrices.translate(.35 - .0625, Math.sin(shardAngle + angle) * .05, -.0625);

                FLOATING_SHARD.render(matrices, consumer, packedLight, overlay);

                matrices.pop();
            }
        }

        matrices.pop();
    }

}
