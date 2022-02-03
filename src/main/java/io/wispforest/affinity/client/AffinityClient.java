package io.wispforest.affinity.client;

import io.wispforest.affinity.client.render.blockentity.AethumFluxCacheBlockEntityRenderer;
import io.wispforest.affinity.client.render.blockentity.AethumFluxNodeBlockEntityRenderer;
import io.wispforest.affinity.client.render.blockentity.BrewingCauldronBlockEntityRenderer;
import io.wispforest.affinity.client.render.entity.WispEntityRenderer;
import io.wispforest.affinity.init.AffinityBlocks;
import io.wispforest.affinity.init.AffinityEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
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
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AZALEA_DOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AZALEA_TRAPDOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.UNFLOWERING_AZALEA_LEAVES, RenderLayer.getCutout());

        EntityRendererRegistry.register(AffinityEntities.PASSIVE_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.HOSTILE_WISP, WispEntityRenderer::new);

        ForcedTexturesLoader.load();
    }
}
