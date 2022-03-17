package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.block.impl.AethumFluxCacheBlock;
import io.wispforest.affinity.blockentity.impl.AethumFluxCacheBlockEntity;
import io.wispforest.affinity.misc.util.MathUtil;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import static io.wispforest.affinity.client.render.blockentity.AethumFluxNodeBlockEntityRenderer.FLOATING_SHARD;

public class AethumFluxCacheBlockEntityRenderer implements BlockEntityRenderer<AethumFluxCacheBlockEntity> {

    private static final Identifier WATER_TEXTURE = new Identifier("block/water_still");

    public AethumFluxCacheBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AethumFluxCacheBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        final float[] rgb = MathUtil.splitRGBToFloats(0x94daff);

        final var cachePart = entity.getCachedState().get(AethumFluxCacheBlock.PART);
        final var bottomY = cachePart.isBase ? 0.25f : 0;
        final var topY = (cachePart.hasCap ? 0.75f : 1);

        final var targetFluxY = bottomY + (entity.flux() / (float) entity.fluxCapacity()) * (topY - bottomY);
        entity.renderFluxY = MathUtil.proportionalApproach(entity.renderFluxY, targetFluxY, .0025f, .0025f);

        final var parent = entity.parent();
        final var noFluxAbove = parent == null || parent.nextIsEmpty();

        if (!entity.tier().isNone() && (entity.flux() > 1 || cachePart.isBase) && noFluxAbove) {
            matrices.push();

            var y = entity.renderFluxY - .125 + Math.sin(System.currentTimeMillis() / 2000d) * .02;
            if (cachePart.isBase) y = Math.max(bottomY, y);
            if (cachePart.hasCap) y = Math.min(y, topY - .25);

            matrices.translate(.4375, y, .4375);

            final var shardConsumer = entity.tier().sprite().getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());
            FLOATING_SHARD.render(matrices, shardConsumer, light, overlay);
            matrices.pop();
        }

        final var consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
        final var sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(WATER_TEXTURE);

        if (entity.flux() > 1) {
            //noinspection ConstantConditions
            var quadEmitter = RendererAccess.INSTANCE.getRenderer().meshBuilder().getEmitter();
            for (var direction : Direction.values()) {
                if (direction.getAxis().isVertical()) continue;
                fluxQuad(direction, quadEmitter, consumer, matrices, sprite, rgb, .13f, bottomY, .87f, entity.renderFluxY, .13f, light, overlay);
            }

            if (targetFluxY != topY || noFluxAbove) {
                fluxQuad(Direction.UP, quadEmitter, consumer, matrices, sprite, rgb, .13f, .13f, .87f, .87f, 1 - entity.renderFluxY, light, overlay);
            }

            if (parent != null && parent.previousIsNotFull()) {
                fluxQuad(Direction.DOWN, quadEmitter, consumer, matrices, sprite, rgb, .13f, .13f, .87f, .87f, bottomY, light, overlay);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void fluxQuad(Direction direction, QuadEmitter emitter, VertexConsumer consumer, MatrixStack matrices, Sprite sprite, float[] rgb, float left, float bottom, float right, float top, float depth, int light, int overlay) {
        emitter.square(direction, left, bottom, right, top, depth);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);

        consumer.quad(matrices.peek(), emitter.toBakedQuad(0, sprite, false), rgb[0], rgb[1], rgb[2], light, overlay);
    }

}
