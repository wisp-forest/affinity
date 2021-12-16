package io.wispforest.affinity.client;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.render.blockentity.AethumFluxNodeBlockEntityRenderer;
import io.wispforest.affinity.client.render.blockentity.BrewingCauldronBlockEntityRenderer;
import io.wispforest.affinity.network.AffinityPackets;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;

@Environment(EnvType.CLIENT)
public class AffinityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.BREWING_CAULDRON, BrewingCauldronBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHUM_FLUX_NODE, AethumFluxNodeBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SUNDIAL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.COPPER_PLATED_AETHUM_FLUX_CACHE, RenderLayer.getCutout());

        AffinityPackets.Client.registerListeners();

        // TODO make this data-driven

        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(Affinity.id("block/crude_aethum_flux_node_shard"));
            registry.register(Affinity.id("block/fairly_attuned_aethum_flux_node_shard"));
            registry.register(Affinity.id("block/mildly_attuned_aethum_flux_node_shard"));
            registry.register(Affinity.id("block/greatly_attuned_aethum_flux_node_shard"));
        });
    }
}
