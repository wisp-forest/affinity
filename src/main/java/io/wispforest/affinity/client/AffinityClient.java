package io.wispforest.affinity.client;

import com.google.common.base.Suppliers;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.RanthraciteWireBlock;
import io.wispforest.affinity.block.impl.RitualSocleBlock;
import io.wispforest.affinity.client.particle.*;
import io.wispforest.affinity.client.render.AbsoluteEnchantmentGlintHandler;
import io.wispforest.affinity.client.render.blockentity.*;
import io.wispforest.affinity.client.render.entity.WispEntityModel;
import io.wispforest.affinity.client.render.entity.WispEntityRenderer;
import io.wispforest.affinity.client.render.item.MangroveBasketItemRenderer;
import io.wispforest.affinity.client.screen.AssemblyAugmentScreen;
import io.wispforest.affinity.client.screen.OuijaBoardScreen;
import io.wispforest.affinity.client.screen.RitualSocleComposerScreen;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.item.IridescenceWandItem;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityEntities;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.hud.Hud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AffinityClient implements ClientModInitializer {

    public static final Identifier LINKING_HUD_ID = Affinity.id("aethum_linking");

    @Override
    public void onInitializeClient() {
        this.registerBlockEntityRenderers();
        this.assignBlockRenderLayers();
        this.registerLinkingHud();

        BuiltinItemRendererRegistry.INSTANCE.register(AffinityBlocks.MANGROVE_BASKET, new MangroveBasketItemRenderer());

        AffinityModelPredicateProviders.applyDefaults();

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            return RanthraciteWireBlock.COLORS[state.get(RanthraciteWireBlock.POWER)];
        }, AffinityBlocks.RANTHRACITE_WIRE);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (tintIndex != 0 || !(state.getBlock() instanceof RitualSocleBlock socle)) return 0xFFFFFF;
            return socle.glowColor();
        }, AffinityBlocks.REFINED_RITUAL_SOCLE, AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE, AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (tintIndex != 1) return 0xFFFFFF;
            return Affinity.AETHUM_FLUX_COLOR;
        }, AffinityBlocks.CREATIVE_AETHUM_FLUX_CACHE);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex != 0) return 0xFFFFFF;

            final var type = RitualSocleType.forBlockItem(stack);
            return type == null ? 0xFFFFFF : type.glowColor();
        }, AffinityBlocks.REFINED_RITUAL_SOCLE, AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE, AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE);

        EntityModelLayerRegistry.registerModelLayer(WispEntityModel.LAYER, WispEntityModel::createModelData);

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

        Hud.add(Affinity.id("player_aethum"), () -> {
            return Components.label(Text.empty())
                    .shadow(true)
                    .margins(Insets.of(5))
                    .positioning(Positioning.relative(0, 50));
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            var possibleComponent = Hud.getComponent(Affinity.id("player_aethum"));
            if (!(possibleComponent instanceof LabelComponent aethumDisplay)) return;

            if (client.player == null) return;
            var aethum = AffinityComponents.PLAYER_AETHUM.get(client.player);

            aethumDisplay.text(Text.literal(
                    "Aethum: "
                            + MathUtil.rounded(aethum.getAethum(), 1) + "/" + MathUtil.rounded(aethum.getMaxAethum(), 1)
                            + " (" + (int) (aethum.getAethum() / aethum.getMaxAethum() * 100) + "%)"
            ));
        });

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

    private void registerLinkingHud() {
        var component = Suppliers.<Component>memoize(() -> {
            return Containers.verticalFlow(Sizing.content(), Sizing.content())
                    .positioning(Positioning.relative(50, 50))
                    .margins(Insets.right(32));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            var stack = client.player.getMainHandStack();
            if (stack.getItem() instanceof IridescenceWandItem wand) {
                if (!Hud.hasComponent(LINKING_HUD_ID)) Hud.add(LINKING_HUD_ID, component);

                var potentialComponent = Hud.getComponent(LINKING_HUD_ID);
                if (!(potentialComponent instanceof FlowLayout container)) return;

                var storedPos = wand.getStoredPos(stack);
                var blockEntity = storedPos != null
                        ? client.world.getBlockEntity(storedPos)
                        : null;

                container.<FlowLayout>configure(layout -> {
                    layout.clearChildren();

                    if (blockEntity != null) {
                        layout.child(Components.block(blockEntity.getCachedState().getBlock().getDefaultState(), blockEntity)
                                .sizing(Sizing.fixed(16)));

                        var linkActionLabel = switch (stack.get(IridescenceWandItem.MODE)) {
                            case BIND -> switch (wand.getType(stack)) {
                                case PUSH -> Text.literal("→").styled(style -> style.withColor(0x3955E5));
                                case NORMAL -> Text.literal("+").styled(style -> style.withColor(0x28FFBF));
                            };
                            case RELEASE -> Text.literal("-").styled(style -> style.withColor(0xEB1D36));
                        };

                        layout.child(Components.label(linkActionLabel)
                                .shadow(true)
                                .positioning(Positioning.relative(100, 100))
                                .zIndex(750));
                    }
                });
            } else {
                Hud.remove(LINKING_HUD_ID);
            }
        });
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
    }
}
