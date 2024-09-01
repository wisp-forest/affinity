package io.wispforest.affinity.client.render.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.AffinityClient;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class VoidResonantEtherealAmethystShardRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private static final Identifier CORE_MODEL_ID = Affinity.id("item/void_resonant_ethereal_amethyst_shard_overlay");
    private static final Identifier OUTLINE_MODEL_ID = Affinity.id("item/void_resonant_ethereal_amethyst_shard_outline");

    private static final RenderLayer END_PORTAL_OVER_TEXTURE = RenderLayer.of(
            "end_portal",
            VertexFormats.POSITION_TEXTURE,
            VertexFormat.DrawMode.QUADS,
            1536,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(new RenderPhase.ShaderProgram(AffinityClient.END_PORTAL_OVER_TEXTURE_PROGRAM::program))
                    .texture(
                            RenderPhase.Textures.create()
                                    .add(EndPortalBlockEntityRenderer.SKY_TEXTURE, false, false)
                                    .add(EndPortalBlockEntityRenderer.PORTAL_TEXTURE, false, false)
                                    .add(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, true)
                                    .build()
                    )
                    .build(false)
    );

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var client = MinecraftClient.getInstance();
        var itemRenderer = client.getItemRenderer();

        var coreModel = client.getBakedModelManager().getModel(CORE_MODEL_ID);
        var outlineModel = client.getBakedModelManager().getModel(OUTLINE_MODEL_ID);

        matrices.push();
        matrices.translate(.5, .5, .5);

        itemRenderer.renderItem(
                stack,
                ModelTransformationMode.NONE, false,
                matrices, vertexConsumers,
                light, overlay,
                outlineModel
        );

        itemRenderer.renderItem(
                stack,
                ModelTransformationMode.NONE, false,
                matrices, layer -> vertexConsumers.getBuffer(END_PORTAL_OVER_TEXTURE),
                light, overlay,
                coreModel
        );

        matrices.pop();
    }
}
