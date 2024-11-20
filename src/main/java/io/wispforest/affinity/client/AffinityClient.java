package io.wispforest.affinity.client;

import dev.emi.trinkets.api.TrinketsApi;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.RanthraciteWireBlock;
import io.wispforest.affinity.block.impl.RitualSocleBlock;
import io.wispforest.affinity.client.hud.AethumNetworkLinkingHud;
import io.wispforest.affinity.client.hud.NimbleStaffHud;
import io.wispforest.affinity.client.hud.PlayerAethumHud;
import io.wispforest.affinity.client.hud.SwivelStaffHud;
import io.wispforest.affinity.client.particle.*;
import io.wispforest.affinity.client.render.AbsoluteEnchantmentGlintHandler;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.client.render.LightLeakRenderer;
import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import io.wispforest.affinity.client.render.blockentity.*;
import io.wispforest.affinity.client.render.entity.*;
import io.wispforest.affinity.client.render.item.*;
import io.wispforest.affinity.client.render.program.*;
import io.wispforest.affinity.client.screen.*;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.item.*;
import io.wispforest.affinity.misc.UnfinishedFeaturesResourceCondition;
import io.wispforest.affinity.misc.callback.PostItemRenderCallback;
import io.wispforest.affinity.misc.callback.ReplaceAttackDamageTextCallback;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.*;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.affinity.particle.ColoredFallingDustParticleEffect;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class AffinityClient implements ClientModInitializer {

    public static final DepthMergeBlitProgram DEPTH_MERGE_BLIT_PROGRAM = new DepthMergeBlitProgram();
    public static final SolidFromFramebufferProgram SOLID_FROM_FRAMEBUFFER = new SolidFromFramebufferProgram();
    public static final DownsampleProgram DOWNSAMPLE_PROGRAM = new DownsampleProgram();
    public static final FizzleProgram EMANCIPATE_BLOCK_PROGRAM = new FizzleProgram(Affinity.id("emancipate_block"));
    public static final FizzleProgram EMANCIPATE_ENTITY_PROGRAM = new FizzleProgram(Affinity.id("emancipate_entity"));
    public static final BlitPostEffectBufferProgram BLIT_POST_EFFECT_BUFFER = new BlitPostEffectBufferProgram();
    public static final EndPortalOverTextureProgram END_PORTAL_OVER_TEXTURE_PROGRAM = new EndPortalOverTextureProgram();

    public static final KeyBinding ACTIVATE_EVADE_RING = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.affinity.activate_evade_ring", GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.movement"));
    public static final KeyBinding SELECT_STAFF_FROM_BUNDLE = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.affinity.select_staff_from_bundle", GLFW.GLFW_KEY_X, "key.categories.inventory"));

    @Override
    public void onInitializeClient() {
        this.registerBlockEntityRenderers();
        this.assignBlockRenderLayers();
        this.registerColorProviders();

        ModelLoadingPlugin.register(ctx -> {
            ctx.addModels(
                Affinity.id("item/staff_bundle"),
                Affinity.id("item/void_resonant_ethereal_amethyst_shard_overlay"),
                Affinity.id("item/void_resonant_ethereal_amethyst_shard_outline"),
                Affinity.id("item/villager_armature_base"),
                VillagerArmatureScreen.CROSSHAIR_MODEL_ID,
                VillagerArmatureScreen.CROSSHAIR_PREVIEW_MODEL_ID
            );
        });

        BuiltinItemRendererRegistry.INSTANCE.register(AffinityBlocks.MANGROVE_BASKET, new MangroveBasketItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(AffinityBlocks.AFFINE_INFUSER, new AffineInfuserBlockEntityRenderer(null));
        BuiltinItemRendererRegistry.INSTANCE.register(AffinityBlocks.FIELD_COHERENCE_MODULATOR, new FieldCoherenceModulatorBlockEntityRenderer(null));
        BuiltinItemRendererRegistry.INSTANCE.register(AffinityItems.VOID_RESONANT_ETHEREAL_AMETHYST_SHARD, new VoidResonantEtherealAmethystShardRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(AffinityBlocks.AZALEA_CHEST, new AzaleaChestItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(AffinityItems.VILLAGER_ARMS, new VillagerArmsItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(AffinityBlocks.VILLAGER_ARMATURE, new VillagerArmatureItemRenderer());

        PostItemRenderCallback.EVENT.register((stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, model, item) -> {
            boolean hasItemGlow = item != null && item.getComponent(AffinityComponents.ENTITY_FLAGS).hasFlag(EntityFlagComponent.ITEM_GLOW);
            if (mode == ModelTransformationMode.GUI || (!stack.isOf(AffinityItems.DRAGON_DROP) && !hasItemGlow)) return;

            if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) immediate.draw();

            matrices.push();
            matrices.translate(.5f, .5f, .5f);

            if (model.hasDepth()) {
                matrices.scale(.03f, .03f, .03f);
            } else {
                matrices.scale(.01f, .01f, .01f);
            }

            LightLeakRenderer.render(
                matrices,
                vertexConsumers,
                hasItemGlow
                    ? Color.WHITE
                    : new Color(.5f, 0f, 1f, 1f)
            );

            matrices.pop();
        });

        PostItemRenderCallback.EVENT.register((stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, item) -> {
            if (!stack.isOf(AffinityItems.CARBON_COPY) || renderMode != ModelTransformationMode.GUI) return;

            var recipeComponent = stack.get(CarbonCopyItem.RECIPE);
            if (recipeComponent == null) return;

            matrices.translate(.75, .25, 1);
            matrices.scale(.5f, .5f, .5f);

            MinecraftClient.getInstance().getItemRenderer().renderItem(
                recipeComponent.result(), renderMode, light, overlay, matrices, vertexConsumers, null, 0
            );
        });

        TooltipComponentCallback.EVENT.register(data -> {
            return data instanceof StaffItem.BundleTooltipData tooltipData
                ? new StaffBundleTooltipComponent(tooltipData)
                : null;
        });

        TooltipComponentCallback.EVENT.register(data -> {
            return data instanceof PhantomBundleItem.StacksTooltipData tooltipData
                ? new PhantomBundleTooltipComponent(tooltipData.stacks())
                : null;
        });

        TooltipComponentCallback.EVENT.register(data -> {
            return data instanceof CarbonCopyItem.TooltipData tooltipData
                ? new CarbonCopyTooltipComponent(tooltipData)
                : null;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ACTIVATE_EVADE_RING.wasPressed()) {
                if (!TrinketsApi.getTrinketComponent(client.player).get().isEquipped(AffinityItems.EVADE_RING)) return;
                if (client.player.getItemCooldownManager().isCoolingDown(AffinityItems.EVADE_RING)) continue;

                var forward = new Vec3d(0, 0, 1).rotateY((float) -Math.toRadians(client.player.headYaw));
                Vec3d direction = null;

                if (client.options.forwardKey.isPressed()) {
                    direction = forward;
                } else if (client.options.backKey.isPressed()) {
                    direction = forward.multiply(-1);
                } else if (client.options.leftKey.isPressed()) {
                    direction = forward.rotateY((float) Math.toRadians(90));
                } else if (client.options.rightKey.isPressed()) {
                    direction = forward.rotateY((float) Math.toRadians(-90));
                }

                if (direction != null && client.player.getComponent(AffinityComponents.PLAYER_AETHUM).tryConsumeAethum(EvadeRingItem.AETHUM_PER_USE)) {
                    AffinityNetwork.CHANNEL.clientHandle().send(new EvadeRingItem.EvadePacket(direction));
                    client.player.getComponent(AffinityComponents.EVADE).evade(direction);
                }
            }

            while (SELECT_STAFF_FROM_BUNDLE.wasPressed()) {
                for (var hand : Hand.values()) {
                    var playerStack = client.player.getStackInHand(hand);
                    if (!(playerStack.getItem() instanceof StaffItem)) continue;

                    var bundle = playerStack.get(StaffItem.BUNDLED_STAFFS);
                    if (bundle == null || bundle.isEmpty()) continue;

                    if (bundle.size() == 1) {
                        AffinityNetwork.CHANNEL.clientHandle().send(new StaffItem.SelectStaffFromBundlePacket(hand, 0));
                    } else {
                        client.setScreen(new SelectStaffFromBundleScreen(hand, playerStack));
                    }
                    break;
                }
            }
        });

        WorldRenderEvents.AFTER_SETUP.register(context -> {
            var player = MinecraftClient.getInstance().player;
            if (!(player.getMainHandStack().getItem() instanceof IridescenceWandItem wandItem)) return;

            var linkOrigin = wandItem.getStoredPos(player.getMainHandStack());
            if (linkOrigin == null) return;

            CuboidRenderer.add(linkOrigin, CuboidRenderer.Cuboid.of(BlockPos.ORIGIN, new BlockPos(1, 1, 1)));
        });

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            if (!stack.isIn(UnfinishedFeaturesResourceCondition.UNFINISHED_ITEMS) || Affinity.config().unfinishedFeatures()) return;
            lines.add(Text.empty());
            lines.add(Text.translatable("text.affinity.unfinished_item_tooltip"));
        });

        AethumNetworkLinkingHud.initialize();
        PlayerAethumHud.initialize();
        NimbleStaffHud.initialize();
        SwivelStaffHud.initialize();
        InWorldTooltipRenderer.initialize();
        AffinityLavenderRecipePreviewBuilders.initialize();

        AffinityModelPredicateProviders.applyDefaults();

        EntityModelLayerRegistry.registerModelLayer(WispEntityModel.LAYER, WispEntityModel::createModelData);
        EntityModelLayerRegistry.registerModelLayer(AsteroidEntityModel.LAYER, AsteroidEntityModel::createModelData);

        HandledScreens.register(AffinityScreenHandlerTypes.RITUAL_SOCLE_COMPOSER, RitualSocleComposerScreen::new);
        HandledScreens.register(AffinityScreenHandlerTypes.ASSEMBLY_AUGMENT, AssemblyAugmentScreen::new);
        HandledScreens.register(AffinityScreenHandlerTypes.OUIJA_BOARD, OuijaBoardScreen::new);
        HandledScreens.register(AffinityScreenHandlerTypes.ITEM_TRANSFER_NODE, ItemTransferNodeScreen::new);
        HandledScreens.register(AffinityScreenHandlerTypes.LARGE_AZALEA_CHEST, LargeAzaleaChestScreen::new);

        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.COLORED_FLAME, ColoredFlamedParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.SMALL_COLORED_FLAME, ColoredFlamedParticle.SmallFactory::new);
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.BEZIER_PATH, new BezierPathParticle.Factory());
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.BEZIER_PATH_EMITTER, new BezierPathEmitterParticle.Factory());
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.GENERIC_EMITTER, new GenericEmitterParticle.Factory());
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.ORBITING_EMITTER, new OrbitingEmitterParticle.Factory());
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.COLORED_FALLING_DUST, ColoredFallingDustParticleEffect.ParticleFactory::new);
        ParticleFactoryRegistry.getInstance().register(AffinityParticleTypes.DIRECTIONAL_SHRIEK, DirectionalShriekParticle.Factory::new);

        EntityRendererRegistry.register(AffinityEntities.INERT_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.WISE_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.VICIOUS_WISP, WispEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.ASTEROID, AsteroidEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.AETHUM_MISSILE, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(AffinityEntities.EMANCIPATED_BLOCK, EmancipatedBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putFluid(AffinityBlocks.Fluids.ARCANE_FADE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putFluid(AffinityBlocks.Fluids.ARCANE_FADE_FLOWING, RenderLayer.getTranslucent());
        FluidRenderHandlerRegistry.INSTANCE.register(AffinityBlocks.Fluids.ARCANE_FADE, AffinityBlocks.Fluids.ARCANE_FADE_FLOWING, SimpleFluidRenderHandler.coloredWater(0xA86464));

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            final var tier = AttunedShardTier.forItem(stack.getItem());
            if (tier.isNone()) return;

            lines.add(Text.translatable("text.affinity.attuned_shard_max_transfer", tier.maxTransfer() * 20));
            lines.add(Text.translatable("text.affinity.attuned_shard_range", tier.maxDistance()));
        });

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                if (!(line.getContent() instanceof PlainTextContent.Literal) || line.getSiblings().isEmpty()) continue;

                var sibling = line.getSiblings().get(0);
                if (!(sibling.getContent() instanceof TranslatableTextContent modifierTranslatable)) continue;
                if (modifierTranslatable.getArgs().length < 2) continue;

                if (!(modifierTranslatable.getArgs()[1] instanceof Text text) || !(text.getContent() instanceof TranslatableTextContent translatable) || !translatable.getKey().startsWith("attribute.name.generic.attack_damage")) {
                    continue;
                }

                var replacement = ReplaceAttackDamageTextCallback.EVENT.invoker().replaceDamageText(stack);
                if (replacement == null) return;

                lines.set(i, replacement.append(Text.translatable(EntityAttributes.GENERIC_ATTACK_DAMAGE.value().getTranslationKey()).formatted(Formatting.DARK_GREEN)));
                return;
            }
        });

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            AbsoluteEnchantmentGlintHandler.reloadLayers(registries);
        });
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
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.RITUAL_SOCLE, ItemSocleBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.BLANK_RITUAL_SOCLE, ItemSocleBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.ASP_RITE_CORE, AspRiteCoreBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.SPIRIT_INTEGRATION_APPARATUS, SpiritIntegrationApparatusBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.MANGROVE_BASKET, MangroveBasketBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.STAFF_PEDESTAL, StaffPedestalBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.ITEM_TRANSFER_NODE, ItemTransferNodeBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.OUIJA_BOARD, OuijaBoardBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.ASSEMBLY_AUGMENT, AssemblyAugmentBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.AFFINE_INFUSER, AffineInfuserBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.VOID_BEACON, VoidBeaconBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.FIELD_COHERENCE_MODULATOR, FieldCoherenceModulatorBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.HOLOGRAPHIC_STEREOPTICON, HolographicStereopticonBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.GRAVITON_TRANSDUCER, GravitonTransducerBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.ARBOREAL_ANNIHILATION_APPARATUS, ArborealAnnihilationApparatusBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.ETHEREAL_AETHUM_FLUX_INJECTOR, EtherealAethumFluxInjectorBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.ETHEREAL_AETHUM_FLUX_NODE, EtherealAethumFluxNodeBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.LOCAL_DISPLACEMENT_GATEWAY, LocalDisplacementGatewayBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.AZALEA_CHEST, ChestBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AffinityBlocks.Entities.VILLAGER_ARMATURE, VillagerArmatureBlockEntityRenderer::new);
    }

    private void assignBlockRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SUNDIAL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AETHUM_FLUX_CACHE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.CREATIVE_AETHUM_FLUX_CACHE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.WORLD_PIN, RenderLayer.getTranslucent());
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
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.AETHUM_PROBE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.ARCANE_TREETAP, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.RITUAL_SOCLE_COMPOSER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.VOID_BEACON, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AffinityBlocks.SONIC_SYPHON, RenderLayer.getCutout());
    }
}
