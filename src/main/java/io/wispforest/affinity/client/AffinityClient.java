package io.wispforest.affinity.client;

import io.wispforest.affinity.client.render.blockentityrenderer.AetherFluxNodeBlockEntityRenderer;
import io.wispforest.affinity.client.render.blockentityrenderer.BrewingCauldronBlockEntityRenderer;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class AffinityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.BREWING_CAULDRON, BrewingCauldronBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHER_FLUX_NODE, AetherFluxNodeBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SUNDIAL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.COPPER_PLATED_AETHER_FLUX_CACHE, RenderLayer.getCutout());
    }
}
