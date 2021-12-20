package io.wispforest.affinity.client;

import io.wispforest.affinity.client.render.ForcedTexturesLoader;
import io.wispforest.affinity.client.render.blockentity.AethumFluxCacheBlockEntityRenderer;
import io.wispforest.affinity.client.render.blockentity.AethumFluxNodeBlockEntityRenderer;
import io.wispforest.affinity.client.render.blockentity.BrewingCauldronBlockEntityRenderer;
import io.wispforest.affinity.network.AffinityPackets;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class AffinityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.BREWING_CAULDRON, BrewingCauldronBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHUM_FLUX_NODE, AethumFluxNodeBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, AethumFluxCacheBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SUNDIAL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AETHUM_FLUX_CACHE, RenderLayer.getCutout());

        AffinityPackets.Client.registerListeners();
        ForcedTexturesLoader.load();
    }
}
