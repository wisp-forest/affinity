package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.AethumFluxNodeBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.minecraft.client.model.*;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.LightType;

public class AethumFluxNodeBlockEntityRenderer implements BlockEntityRenderer<AethumFluxNodeBlockEntity> {

    private static final SpriteIdentifier SHARD_TEXTURE =
            new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Affinity.id("block/aethum_flux_node_shard"));

    private static final ModelPart FLOATING_SHARD;

    static {
        ModelData floatingShardData = new ModelData();
        floatingShardData.getRoot().addChild("floatingShard", ModelPartBuilder.create()
                .cuboid(0, 0, 0, 2, 4, 2)
                .uv(0, 0), ModelTransform.NONE);
        FLOATING_SHARD = TexturedModelData.of(floatingShardData, 16, 16).createModel();
    }

    public AethumFluxNodeBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AethumFluxNodeBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        for (var linkedMember : entity.linkedMembers()) {
            var offset = Vec3d.ofCenter(linkedMember).subtract(Vec3d.of(entity.getPos()));

            vertexConsumers.getBuffer(RenderLayer.LINES)
                    .vertex(matrices.peek().getPositionMatrix(), .5f, .5f, .5f)
                    .color(0xff8800ff)
                    .normal(1, 0, 1).next();
            vertexConsumers.getBuffer(RenderLayer.LINES)
                    .vertex(matrices.peek().getPositionMatrix(), (float) offset.x, (float) offset.y, (float) offset.z)
                    .color(0xffff0000)
                    .normal(1, 0, 1).next();
        }

        if (entity.getCachedState().isOf(AffinityBlocks.COPPER_PLATED_AETHUM_FLUX_NODE)) {
            var packedLight = LightmapTextureManager.pack(
                    entity.getWorld().getLightLevel(LightType.BLOCK, entity.getPos()) - 2,
                    entity.getWorld().getLightLevel(LightType.SKY, entity.getPos()));

            entity.getWorld().random.setSeed(entity.getPos().asLong());
            long time = System.currentTimeMillis() + entity.getWorld().random.nextLong(5000);

            float angle = (float) ((time / 2000d) % (2 * Math.PI));

            matrices.push();

            double shardHeight = Math.sin(time / 1000d) * .015;
            matrices.translate(0.4375, .8 + shardHeight, 0.4375);

            matrices.push();

            matrices.translate(.0625, 0, .0625);
            matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(-/*20 * */angle));
            matrices.translate(-.0625, 0, -.0625);

            var consumer = SHARD_TEXTURE.getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());
            FLOATING_SHARD.render(matrices, consumer, packedLight, overlay);

            matrices.pop();

            matrices.translate(0, -shardHeight, 0);

            for (int i = 0; i < 5; i++) {
                matrices.push();

                var shardAngle = (float) (angle /* * 20*/ + i * (2 / 5f) * Math.PI);

                matrices.translate(.0625, 0, .0625);
                matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(shardAngle));
                matrices.translate(.35 - .0625, Math.sin(shardAngle + angle) * .05, -.0625);

                FLOATING_SHARD.render(matrices, consumer, packedLight, overlay);

                matrices.pop();
            }

            matrices.pop();
        }

        ((VertexConsumerProvider.Immediate) vertexConsumers).draw();
    }

}
