package com.glisco.nidween.client;

import com.glisco.nidween.client.render.blockentityrenderer.BrewingCauldronBlockEntityRenderer;
import com.glisco.nidween.registries.NidweenBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class NidweenClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(NidweenBlocks.Entities.BREWING_CAULDRON, BrewingCauldronBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(NidweenBlocks.SUNDIAL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NidweenBlocks.COPPER_PLATED_AETHER_FLUX_CACHE, RenderLayer.getCutout());
    }
}
