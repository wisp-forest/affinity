package com.glisco.nidween.client;

import com.glisco.nidween.client.render.blockentityrenderer.BrewingCauldronBlockEntityRenderer;
import com.glisco.nidween.registries.NidweenBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class NidweenClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        BlockEntityRendererRegistry.INSTANCE.register(NidweenBlocks.BlockEntityTypes.BREWING_CAULDRON, BrewingCauldronBlockEntityRenderer::new);

    }
}
