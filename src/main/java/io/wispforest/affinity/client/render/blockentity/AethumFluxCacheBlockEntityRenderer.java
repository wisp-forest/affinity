package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.block.impl.AethumFluxCacheBlock;
import io.wispforest.affinity.blockentity.impl.AethumFluxCacheBlockEntity;
import io.wispforest.affinity.util.MathUtil;
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

public class AethumFluxCacheBlockEntityRenderer implements BlockEntityRenderer<AethumFluxCacheBlockEntity> {

    private static final Identifier WATER_TEXTURE = new Identifier("block/water_still");

    public AethumFluxCacheBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AethumFluxCacheBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.flux() < 1) return;

        final int color = 0xA685E2;
        final float[] rgb = {(color >> 16) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f};

        final var consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
        final var sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(WATER_TEXTURE);

        final var cachePart = entity.getCachedState().get(AethumFluxCacheBlock.PART);
        final var bottomY = cachePart.isBase ? 0.125f : 0;
        final var topY = (cachePart.hasCap ? 0.875f : 1);

        final var targetFluxY = bottomY + (entity.flux() / (float) entity.fluxCapacity()) * (topY - bottomY);
        entity.renderFluxY = MathUtil.proportionalApproach(entity.renderFluxY, targetFluxY, .0025f, .0075f);

        //noinspection ConstantConditions
        var quadEmitter = RendererAccess.INSTANCE.getRenderer().meshBuilder().getEmitter();
        for (var direction : Direction.values()) {
            if (direction.getAxis().isVertical()) continue;
            fluxQuad(direction, quadEmitter, consumer, matrices, sprite, rgb, .13f, bottomY, .87f, entity.renderFluxY, .13f, light, overlay);
        }

        final var parent = entity.parent();

        if (targetFluxY != topY || parent == null || parent.nextIsEmpty()) {
            fluxQuad(Direction.UP, quadEmitter, consumer, matrices, sprite, rgb, .13f, .13f, .87f, .87f, 1 - entity.renderFluxY, light, overlay);
        }

        if (parent != null && parent.previousIsNotFull()) {
            fluxQuad(Direction.DOWN, quadEmitter, consumer, matrices, sprite, rgb, .13f, .13f, .87f, .87f, bottomY, light, overlay);
        }

    }

    private static void fluxQuad(Direction direction, QuadEmitter emitter, VertexConsumer consumer, MatrixStack matrices, Sprite sprite, float[] rgb, float left, float bottom, float right, float top, float depth, int light, int overlay) {
        emitter.square(direction, left, bottom, right, top, depth);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        consumer.quad(matrices.peek(), emitter.toBakedQuad(0, sprite, false), rgb[0], rgb[1], rgb[2], light, overlay);
    }

}
