package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.block.impl.AethumFluxCacheBlock;
import io.wispforest.affinity.blockentity.impl.AethumFluxCacheBlockEntity;
import io.wispforest.affinity.util.MathUtil;
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
import net.minecraft.util.math.Matrix4f;

public class AethumFluxCacheBlockEntityRenderer implements BlockEntityRenderer<AethumFluxCacheBlockEntity> {

    private static final Identifier WATER_TEXTURE = new Identifier("block/water_still");

    public AethumFluxCacheBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AethumFluxCacheBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.flux() < 1) return;

        var consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
        var sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(WATER_TEXTURE);

        final var part = entity.getCachedState().get(AethumFluxCacheBlock.PART);
        var bottomY = part.isBase ? 0.125f : 0;
        var topY = (part == AethumFluxCacheBlock.Part.TOP || part == AethumFluxCacheBlock.Part.STANDALONE ? 0.875f : 1) - bottomY;

        var targetTopY = bottomY + (entity.flux() / (float) entity.fluxCapacity()) * topY;

        entity.renderHeight = MathUtil.proportionalApproach(entity.renderHeight, targetTopY, .0025f, .0075f);
        targetTopY = entity.renderHeight;

        sideQuad(sprite, matrices, consumer, .87f, bottomY, .13f, .87f, targetTopY, .87f, 0x6E3CBC, overlay, light);
        sideQuad(sprite, matrices, consumer, .13f, bottomY, .87f, .13f, targetTopY, .13f, 0x6E3CBC, overlay, light);
        sideQuad(sprite, matrices, consumer, .13f, bottomY, .13f, .87f, targetTopY, .13f, 0x6E3CBC, overlay, light);
        sideQuad(sprite, matrices, consumer, .87f, bottomY, .87f, .13f, targetTopY, .87f, 0x6E3CBC, overlay, light);

        final var parent = entity.parent();

        if (targetTopY != topY + bottomY || parent == null || (parent.next() != null && parent.next().flux() == 0)) {
            topQuad(sprite, matrices, consumer, targetTopY, .13f, .13f, .87f, .87f, 0x6E3CBC, overlay, light);
        }

        if (parent == null) return;

        final var previous = parent.previous();
        if (previous != null && previous.flux() < previous.fluxCapacity()) {
            consumer.vertex(matrices.peek().getPositionMatrix(), 0.13f, bottomY, 0.13f).color(148, 179, 253, 255).texture(sprite.getFrameU(0), sprite.getFrameV(0)).overlay(overlay).light(light).normal(0, 1, 0).next();
            consumer.vertex(matrices.peek().getPositionMatrix(), 0.87f, bottomY, 0.13f).color(148, 179, 253, 255).texture(sprite.getFrameU(16), sprite.getFrameV(0)).overlay(overlay).light(light).normal(0, 1, 0).next();
            consumer.vertex(matrices.peek().getPositionMatrix(), 0.87f, bottomY, 0.87f).color(148, 179, 253, 255).texture(sprite.getFrameU(16), sprite.getFrameV(16)).overlay(overlay).light(light).normal(0, 1, 0).next();
            consumer.vertex(matrices.peek().getPositionMatrix(), 0.13f, bottomY, 0.87f).color(148, 179, 253, 255).texture(sprite.getFrameU(0), sprite.getFrameV(16)).overlay(overlay).light(light).normal(0, 1, 0).next();
        }

    }

    private static void sideQuad(Sprite sprite, MatrixStack matrices, VertexConsumer consumer, float blX, float blY, float blZ, float trX, float trY, float trZ, int color, int overlay, int light) {
        final int[] rgb = {color >> 16, (color >> 8) & 0xFF, color & 0xFF};
        final Matrix4f matrix = matrices.peek().getPositionMatrix();

        consumer.vertex(matrix, blX, blY, blZ).color(rgb[0], rgb[1], rgb[2], 255).texture(sprite.getFrameU(blY * 16), sprite.getFrameV(0)).overlay(overlay).light(light).normal(0, 1, 0).next();
        consumer.vertex(matrix, blX, trY, blZ).color(rgb[0], rgb[1], rgb[2], 255).texture(sprite.getFrameU(trY * 16), sprite.getFrameV(0)).overlay(overlay).light(light).normal(0, 1, 0).next();
        consumer.vertex(matrix, trX, trY, trZ).color(rgb[0], rgb[1], rgb[2], 255).texture(sprite.getFrameU(trY * 16), sprite.getFrameV(16)).overlay(overlay).light(light).normal(0, 1, 0).next();
        consumer.vertex(matrix, trX, blY, trZ).color(rgb[0], rgb[1], rgb[2], 255).texture(sprite.getFrameU(blY * 16), sprite.getFrameV(16)).overlay(overlay).light(light).normal(0, 1, 0).next();
    }

    private static void topQuad(Sprite sprite, MatrixStack matrices, VertexConsumer consumer, float y, float blX, float blZ, float trX, float trZ, int color, int overlay, int light) {
        final int[] rgb = {color >> 16, (color >> 8) & 0xFF, color & 0xFF};
        final Matrix4f matrix = matrices.peek().getPositionMatrix();

        consumer.vertex(matrix, blX, y, blZ).color(rgb[0], rgb[1], rgb[2], 255).texture(sprite.getFrameU(0), sprite.getFrameV(0)).overlay(overlay).light(light).normal(0, 1, 0).next();
        consumer.vertex(matrix, blX, y, trZ).color(rgb[0], rgb[1], rgb[2], 255).texture(sprite.getFrameU(16), sprite.getFrameV(0)).overlay(overlay).light(light).normal(0, 1, 0).next();
        consumer.vertex(matrix, trX, y, trZ).color(rgb[0], rgb[1], rgb[2], 255).texture(sprite.getFrameU(16), sprite.getFrameV(16)).overlay(overlay).light(light).normal(0, 1, 0).next();
        consumer.vertex(matrix, trX, y, blZ).color(rgb[0], rgb[1], rgb[2], 255).texture(sprite.getFrameU(0), sprite.getFrameV(16)).overlay(overlay).light(light).normal(0, 1, 0).next();
    }
}
