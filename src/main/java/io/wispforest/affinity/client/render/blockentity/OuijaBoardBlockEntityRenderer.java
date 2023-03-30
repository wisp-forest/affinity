package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.OuijaBoardBlockEntity;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class OuijaBoardBlockEntityRenderer implements BlockEntityRenderer<OuijaBoardBlockEntity> {

    public static final ModelPart FLOATING_BOARD;
    public static final SpriteIdentifier BOARD_SPRITE_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Affinity.id("block/ouija_board_board"));

    static {
        ModelData floatingShardData = new ModelData();
        floatingShardData.getRoot().addChild("floatingBoard", ModelPartBuilder.create()
                .cuboid(1, 0, 1, 14, 1, 14)
                .uv(0, 0), ModelTransform.NONE);
        FLOATING_BOARD = TexturedModelData.of(floatingShardData, 64, 16).createModel();
    }

    public OuijaBoardBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(OuijaBoardBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        boolean hasPlayers = entity.getWorld().isPlayerInRange(entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ(), 5);
        entity.boardHeight += Delta.compute(entity.boardHeight, hasPlayers ? 1f : 0f, MinecraftClient.getInstance().getLastFrameDuration() * .05);
        entity.time += MinecraftClient.getInstance().getLastFrameDuration() * entity.boardHeight;

        matrices.push();
        matrices.translate(0, .75 + entity.boardHeight * .2f, 0);

        matrices.translate(.5, 0, .5);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(2f * (float) (entity.time)));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(5f * entity.boardHeight));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-3f * (float) entity.time));

        matrices.translate(-.5, 0, -.5);

        var consumer = BOARD_SPRITE_ID.getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());
        FLOATING_BOARD.render(matrices, consumer, light, overlay);

        matrices.pop();
    }
}
