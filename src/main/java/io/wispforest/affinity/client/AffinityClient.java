package io.wispforest.affinity.client;

import io.wispforest.affinity.block.impl.RitualSocleBlock;
import io.wispforest.affinity.client.particle.BezierItemEmitterParticle;
import io.wispforest.affinity.client.particle.BezierItemParticle;
import io.wispforest.affinity.client.particle.ColoredFlamedParticle;
import io.wispforest.affinity.client.render.blockentity.*;
import io.wispforest.affinity.client.render.entity.WispEntityModel;
import io.wispforest.affinity.client.render.entity.WispEntityRenderer;
import io.wispforest.affinity.client.screen.RitualSocleComposerScreen;
import io.wispforest.affinity.item.WispMatterItem;
import io.wispforest.affinity.object.*;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class AffinityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.BREWING_CAULDRON, BrewingCauldronBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHUM_FLUX_NODE, AethumFluxNodeBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, AethumFluxCacheBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.RITUAL_SOCLE, RitualSocleBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.RITUAL_CORE, RitualCoreBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SUNDIAL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AETHUM_FLUX_CACHE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AZALEA_DOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AZALEA_TRAPDOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.UNFLOWERING_AZALEA_LEAVES, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.ASP_RITE_CORE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.REFINED_RITUAL_SOCLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE, RenderLayer.getCutout());

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (tintIndex != 0 || !(state.getBlock() instanceof RitualSocleBlock socle)) return 0xFFFFFF;
            return socle.glowColor();
        }, AffinityBlocks.REFINED_RITUAL_SOCLE, AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE, AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex != 0) return 0xFFFFFF;

            final var type = RitualSocleType.forBlockItem(stack);
            return type == null ? 0xFFFFFF : type.glowColor();
        }, AffinityBlocks.REFINED_RITUAL_SOCLE, AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE, AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE);

        EntityModelLayerRegistry.registerModelLayer(WispEntityModel.LAYER, WispEntityModel::createModelData);

        ScreenRegistry.register(AffinityScreenHandlerTypes.RITUAL_SOCLE_COMPOSER, RitualSocleComposerScreen::new);

        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.COLORED_FLAME, ColoredFlamedParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.BEZIER_ITEM, new BezierItemParticle.Factory());
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.BEZIER_ITEM_EMITTER, new BezierItemEmitterParticle.Factory());

        EntityRendererRegistry.register(AffinityEntities.INERT_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.WISE_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.VICIOUS_WISP, WispEntityRenderer::new);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> ((WispMatterItem) stack.getItem()).wispType().color(),
                AffinityItems.INERT_WISP_MATTER, AffinityItems.WISE_WISP_MATTER, AffinityItems.VICIOUS_WISP_MATTER);

        ForcedTexturesLoader.load();
    }
}
