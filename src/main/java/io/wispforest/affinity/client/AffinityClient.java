package io.wispforest.affinity.client;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.RanthraciteWireBlock;
import io.wispforest.affinity.block.impl.RitualSocleBlock;
import io.wispforest.affinity.client.particle.*;
import io.wispforest.affinity.client.render.*;
import io.wispforest.affinity.client.render.blockentity.*;
import io.wispforest.affinity.client.render.entity.AsteroidEntityModel;
import io.wispforest.affinity.client.render.entity.AsteroidEntityRenderer;
import io.wispforest.affinity.client.render.entity.WispEntityModel;
import io.wispforest.affinity.client.render.entity.WispEntityRenderer;
import io.wispforest.affinity.client.render.item.MangroveBasketItemRenderer;
import io.wispforest.affinity.client.screen.AssemblyAugmentScreen;
import io.wispforest.affinity.client.screen.OuijaBoardScreen;
import io.wispforest.affinity.client.screen.RitualSocleComposerScreen;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.object.*;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
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
        PostItemRenderCallback.EVENT.register((stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, model, item) -> {
            boolean hasItemGlow = item != null && AffinityComponents.ENTITY_FLAGS.get(item).hasFlag(EntityFlagComponent.ITEM_GLOW);
            if (mode != ModelTransformation.Mode.GROUND || (!stack.isOf(AffinityItems.DRAGON_DROP) && !hasItemGlow)) return;

            ((VertexConsumerProvider.Immediate) vertexConsumers).draw();

            matrices.push();
            matrices.translate(.5f, .5f, .5f);
            matrices.scale(.01f, .01f, .01f);

            LightLeakRenderer.render(
                    matrices,
                    vertexConsumers,
                    hasItemGlow
                            ? Color.WHITE
                            : new Color(.5f, 0f, 1f, 1f)
            );

            matrices.pop();
        });

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
        EntityRendererRegistry.register(AffinityEntities.AETHUM_MISSILE, EmptyEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putFluid(AffinityBlocks.Fluids.ARCANE_FADE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putFluid(AffinityBlocks.Fluids.ARCANE_FADE_FLOWING, RenderLayer.getTranslucent());
        FluidRenderHandlerRegistry.INSTANCE.register(AffinityBlocks.Fluids.ARCANE_FADE, AffinityBlocks.Fluids.ARCANE_FADE_FLOWING, SimpleFluidRenderHandler.coloredWater(0xA86464));

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            final var tier = AttunedShardTier.forItem(stack.getItem());
            if (tier.isNone()) return;

            lines.add(Text.translatable("text.affinity.attuned_shard_max_transfer").formatted(Formatting.GRAY)
                    .append(Text.translatable("text.affinity.attuned_shard_max_transfer.value", tier.maxTransfer())
                            .styled(style -> style.withColor(0x4D4C7D))));
            lines.add(Text.translatable("text.affinity.attuned_shard_range").formatted(Formatting.GRAY)
                    .append(Text.translatable("text.affinity.attuned_shard_range.value", tier.maxDistance())
                            .styled(style -> style.withColor(0x4D4C7D))));
        });

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                if (!(line.getContent() instanceof LiteralTextContent) || line.getSiblings().isEmpty()) continue;

                var sibling = line.getSiblings().get(0);
                if (!(sibling.getContent() instanceof TranslatableTextContent modifierTranslatable)) continue;
                if (modifierTranslatable.getArgs().length < 2) continue;

                if (!(modifierTranslatable.getArgs()[1] instanceof Text text) || !(text.getContent() instanceof TranslatableTextContent translatable) || !translatable.getKey().startsWith("attribute.name.generic.attack_damage")) {
                    continue;
                }

                var replacement = ReplaceAttackDamageTextCallback.EVENT.invoker().replaceDamageText(stack);
                if (replacement == null) return;

                lines.set(i, replacement.append(Text.translatable(EntityAttributes.GENERIC_ATTACK_DAMAGE.getTranslationKey()).formatted(Formatting.DARK_GREEN)));
                return;
            }
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
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.BREWING_CAULDRON, BrewingCauldronBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.AETHUM_FLUX_NODE, AethumFluxNodeBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, AethumFluxCacheBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.RITUAL_SOCLE, RitualSocleBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.ASP_RITE_CORE, AspRiteCoreBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.ABERRANT_CALLING_CORE, AberrantCallingCoreBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.MANGROVE_BASKET, MangroveBasketBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.STAFF_PEDESTAL, StaffPedestalBlockEntityRenderer::new);
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
