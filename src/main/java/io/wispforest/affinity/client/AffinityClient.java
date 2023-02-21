package io.wispforest.affinity.client;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.RanthraciteWireBlock;
import io.wispforest.affinity.block.impl.RitualSocleBlock;
import io.wispforest.affinity.client.particle.*;
import io.wispforest.affinity.client.render.AbsoluteEnchantmentGlintHandler;
import io.wispforest.affinity.client.render.SkyBlitProgram;
import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import io.wispforest.affinity.client.render.blockentity.*;
import io.wispforest.affinity.client.render.entity.AsteroidEntityModel;
import io.wispforest.affinity.client.render.entity.AsteroidEntityRenderer;
import io.wispforest.affinity.client.render.entity.WispEntityModel;
import io.wispforest.affinity.client.render.entity.WispEntityRenderer;
import io.wispforest.affinity.client.render.item.MangroveBasketItemRenderer;
import io.wispforest.affinity.client.screen.AssemblyAugmentScreen;
import io.wispforest.affinity.client.screen.OuijaBoardScreen;
import io.wispforest.affinity.client.screen.RitualSocleComposerScreen;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityEntities;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class AffinityClient implements ClientModInitializer {

    public static final SkyBlitProgram SKY_BLIT_PROGRAM = new SkyBlitProgram();

    @Override
    public void onInitializeClient() {
        this.registerBlockEntityRenderers();
        this.assignBlockRenderLayers();
        this.registerColorProviders();

        BuiltinItemRendererRegistry.INSTANCE.register(AffinityBlocks.MANGROVE_BASKET, new MangroveBasketItemRenderer());

        AethumNetworkLinkingHud.initialize();
        PlayerAethumHud.initialize();
        StatProviderRenderer.initialize();

        AffinityModelPredicateProviders.applyDefaults();

        EntityModelLayerRegistry.registerModelLayer(WispEntityModel.LAYER, WispEntityModel::createModelData);
        EntityModelLayerRegistry.registerModelLayer(AsteroidEntityModel.LAYER, AsteroidEntityModel::createModelData);

        HandledScreens.register(AffinityScreenHandlerTypes.RITUAL_SOCLE_COMPOSER, RitualSocleComposerScreen::new);
        HandledScreens.register(AffinityScreenHandlerTypes.ASSEMBLY_AUGMENT, AssemblyAugmentScreen::new);
        HandledScreens.register(AffinityScreenHandlerTypes.OUIJA_BOARD, OuijaBoardScreen::new);

        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.COLORED_FLAME, ColoredFlamedParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.SMALL_COLORED_FLAME, ColoredFlamedParticle.SmallFactory::new);
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.BEZIER_PATH, new BezierPathParticle.Factory());
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.BEZIER_PATH_EMITTER, new BezierPathEmitterParticle.Factory());
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.GENERIC_EMITTER, new GenericEmitterParticle.Factory());
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.ORBITING_EMITTER, new OrbitingEmitterParticle.Factory());

        EntityRendererRegistry.register(AffinityEntities.INERT_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.WISE_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.VICIOUS_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.ASTEROID, AsteroidEntityRenderer::new);

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            final var tier = AttunedShardTiers.forItem(stack.getItem());
            if (tier.isNone()) return;

            lines.add(Text.translatable("text.affinity.attuned_shard_max_transfer").formatted(Formatting.GRAY)
                    .append(Text.translatable("text.affinity.attuned_shard_max_transfer.value", tier.maxTransfer())
                            .styled(style -> style.withColor(0x4D4C7D))));
            lines.add(Text.translatable("text.affinity.attuned_shard_range").formatted(Formatting.GRAY)
                    .append(Text.translatable("text.affinity.attuned_shard_range.value", tier.maxDistance())
                            .styled(style -> style.withColor(0x4D4C7D))));
        });

        AbsoluteEnchantmentGlintHandler.createLayers();
    }

    private void registerColorProviders() {
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            return RanthraciteWireBlock.COLORS[state.get(RanthraciteWireBlock.POWER)];
        }, AffinityBlocks.RANTHRACITE_WIRE);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (tintIndex != 0 || !(state.getBlock() instanceof RitualSocleBlock socle)) return 0xFFFFFF;
            return socle.glowColor();
        }, AffinityBlocks.REFINED_RITUAL_SOCLE, AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE, AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (tintIndex != 1) return 0xFFFFFF;
            return Affinity.AETHUM_FLUX_COLOR.rgb();
        }, AffinityBlocks.CREATIVE_AETHUM_FLUX_CACHE);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex != 0) return 0xFFFFFF;

            final var type = RitualSocleType.forBlockItem(stack);
            return type == null ? 0xFFFFFF : type.glowColor();
        }, AffinityBlocks.REFINED_RITUAL_SOCLE, AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE, AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE);
    }

    private void registerBlockEntityRenderers() {
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.BREWING_CAULDRON, BrewingCauldronBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHUM_FLUX_NODE, AethumFluxNodeBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, AethumFluxCacheBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.RITUAL_SOCLE, RitualSocleBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.ASP_RITE_CORE, AspRiteCoreBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.ABERRANT_CALLING_CORE, AberrantCallingCoreBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.MANGROVE_BASKET, MangroveBasketBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(AffinityBlocks.Entities.STAFF_PEDESTAL, StaffPedestalBlockEntityRenderer::new);
    }

    private void assignBlockRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SUNDIAL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AETHUM_FLUX_CACHE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.CREATIVE_AETHUM_FLUX_CACHE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AZALEA_DOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AZALEA_TRAPDOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.UNFLOWERING_AZALEA_LEAVES, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.ASP_RITE_CORE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.REFINED_RITUAL_SOCLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.RANTHRACITE_WIRE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.MANGROVE_BASKET, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.THE_SKY, SkyCaptureBuffer.SKY_STENCIL_LAYER);
    }
}
