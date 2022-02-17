package io.wispforest.affinity.client;

import io.wispforest.affinity.client.particle.BezierItemParticle;
import io.wispforest.affinity.client.particle.ColoredFlamedParticle;
import io.wispforest.affinity.client.render.blockentity.AethumFluxCacheBlockEntityRenderer;
import io.wispforest.affinity.client.render.blockentity.AethumFluxNodeBlockEntityRenderer;
import io.wispforest.affinity.client.render.blockentity.BrewingCauldronBlockEntityRenderer;
import io.wispforest.affinity.client.render.blockentity.RitualStandBlockEntityRenderer;
import io.wispforest.affinity.client.render.entity.WispEntityModel;
import io.wispforest.affinity.client.render.entity.WispEntityRenderer;
import io.wispforest.affinity.item.WispMatterItem;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityEntities;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class AffinityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.BREWING_CAULDRON, BrewingCauldronBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHUM_FLUX_NODE, AethumFluxNodeBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, AethumFluxCacheBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.RITUAL_STAND, RitualStandBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SUNDIAL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AETHUM_FLUX_CACHE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AZALEA_DOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AZALEA_TRAPDOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.UNFLOWERING_AZALEA_LEAVES, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.ASP_RITE_CORE, RenderLayer.getCutout());

        EntityModelLayerRegistry.registerModelLayer(WispEntityModel.LAYER, WispEntityModel::createModelData);

        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.COLORED_FLAME, ColoredFlamedParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.BEZIER_ITEM, new BezierItemParticle.Factory());

        EntityRendererRegistry.register(AffinityEntities.INERT_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.WISE_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.VICIOUS_WISP, WispEntityRenderer::new);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> ((WispMatterItem) stack.getItem()).wispType().color(),
                AffinityItems.INERT_WISP_MATTER, AffinityItems.WISE_WISP_MATTER, AffinityItems.VICIOUS_WISP_MATTER);

        ForcedTexturesLoader.load();
    }
}
