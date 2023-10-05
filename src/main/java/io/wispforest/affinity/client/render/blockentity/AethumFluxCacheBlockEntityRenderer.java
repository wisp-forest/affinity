package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.AethumFluxCacheBlock;
import io.wispforest.affinity.blockentity.impl.AethumFluxCacheBlockEntity;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import static io.wispforest.affinity.client.render.blockentity.AethumFluxNodeBlockEntityRenderer.FLOATING_SHARD;

public class AethumFluxCacheBlockEntityRenderer extends AffinityBlockEntityRenderer<AethumFluxCacheBlockEntity> {

    private static final SpriteIdentifier WATER_TEXTURE = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("block/water_still"));

    public AethumFluxCacheBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected void render(AethumFluxCacheBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var cachePart = entity.getCachedState().get(AethumFluxCacheBlock.PART);
        var bottomY = cachePart.isBase ? 0.25f : 0;
        var topY = (cachePart.hasCap ? 0.75f : 1);

        var targetFluxY = bottomY + (entity.flux() / (float) entity.fluxCapacity()) * (topY - bottomY);
        entity.renderFluxY += Delta.compute(entity.renderFluxY, targetFluxY, frameDelta);

        var parent = entity.parentRef();
        var noFluxAbove = parent == null || parent.nextIsEmpty();

        if (!entity.tier().isNone() && (entity.flux() > 1 || cachePart.isBase) && noFluxAbove) {
            matrices.push();

            var y = entity.renderFluxY - .125 + Math.sin(time / 2000d) * .02;
            if (cachePart.isBase) y = Math.max(bottomY, y);
            if (cachePart.hasCap) y = Math.min(y, topY - .25);

            matrices.translate(.4375, y, .4375);

            FLOATING_SHARD.render(
                    matrices,
                    entity.tier().sprite().getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid()),
                    light,
                    overlay
            );

            matrices.pop();
        }

        var consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
        var sprite = WATER_TEXTURE.getSprite();

        if (entity.flux() > 1) {
            //noinspection ConstantConditions
            var quadEmitter = RendererAccess.INSTANCE.getRenderer().meshBuilder().getEmitter();
            for (var direction : Direction.values()) {
                if (direction.getAxis().isVertical()) continue;
                fluxQuad(direction, quadEmitter, consumer, matrices, sprite, .13f, bottomY, .87f, entity.renderFluxY, .13f, light, overlay);
            }

            if (targetFluxY != topY || noFluxAbove) {
                fluxQuad(Direction.UP, quadEmitter, consumer, matrices, sprite, .13f, .13f, .87f, .87f, 1 - entity.renderFluxY, light, overlay);
            }

            if (parent != null && parent.previousIsNotFull()) {
                fluxQuad(Direction.DOWN, quadEmitter, consumer, matrices, sprite, .13f, .13f, .87f, .87f, bottomY, light, overlay);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void fluxQuad(Direction direction, QuadEmitter emitter, VertexConsumer consumer, MatrixStack matrices, Sprite sprite, float left, float bottom, float right, float top, float depth, int light, int overlay) {
        emitter.square(direction, left, bottom, right, top, depth);
        emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);

        consumer.quad(
                matrices.peek(),
                emitter.toBakedQuad(sprite),
                Affinity.AETHUM_FLUX_COLOR.red(),
                Affinity.AETHUM_FLUX_COLOR.green(),
                Affinity.AETHUM_FLUX_COLOR.blue(),
                light, overlay
        );
    }

}
