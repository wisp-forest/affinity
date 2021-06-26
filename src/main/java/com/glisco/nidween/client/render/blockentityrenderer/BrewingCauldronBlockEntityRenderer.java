package com.glisco.nidween.client.render.blockentityrenderer;

import com.glisco.nidween.block.BrewingCauldronBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BrewingCauldronBlockEntityRenderer implements BlockEntityRenderer<BrewingCauldronBlockEntity> {

    private static final SpriteIdentifier WATER_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("block/water_still"));
    private static final ModelPart POTION_MODEL;

    static {
        ModelData data = new ModelData();
        data.getRoot().addChild("portionFluid", ModelPartBuilder.create().uv(-24, 0).cuboid(0, 0, 0, 12, 0, 12), ModelTransform.NONE);
        POTION_MODEL = TexturedModelData.of(data, 16, 16).createModel();
    }

    public BrewingCauldronBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

    @Override
    public void render(BrewingCauldronBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        if (!entity.getCurrentPotion().isEmpty()) {
            matrices.push();
            matrices.translate(0.125, entity.getFluidHeight(), 0.125);

            VertexConsumer consumer = WATER_TEXTURE.getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getTranslucent());

            int color = entity.getCurrentPotion().getColor();

            float r = (color >> 16) / 255f;
            float g = ((color & 0xFF00) >> 8) / 255f;
            float b = (color & 0xFF) / 255f;

            POTION_MODEL.render(matrices, consumer, light, overlay, r, g, b, 1);

            matrices.pop();
        }

    }
}
