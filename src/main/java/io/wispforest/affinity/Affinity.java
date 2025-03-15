package io.wispforest.affinity;

import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.enchantment.*;
import io.wispforest.affinity.entity.EmancipatedBlockEntity;
import io.wispforest.affinity.item.AffinityItemGroup;
import io.wispforest.affinity.item.EchoShardExtension;
import io.wispforest.affinity.misc.*;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.*;
import io.wispforest.affinity.worldgen.AffinityStructures;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import io.wispforest.endec.Endec;
import io.wispforest.owo.Owo;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantedCountIncreaseLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Affinity implements ModInitializer {

    public static final String MOD_ID = "affinity";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final io.wispforest.affinity.AffinityConfig CONFIG = io.wispforest.affinity.AffinityConfig.createAndLoad();

    public static final Color AETHUM_FLUX_COLOR = Color.ofRgb(0x6A67CE);

    public static final BlockApiLookup<AethumNetworkMember, Void> AETHUM_MEMBER = BlockApiLookup.get(id("aethum_member"), AethumNetworkMember.class, Void.class);
    public static final BlockApiLookup<AethumNetworkNode, Void> AETHUM_NODE = BlockApiLookup.get(id("aethum_node"), AethumNetworkNode.class, Void.class);

    @Override
    public void onInitialize() {
        AffinityItemGroup.register();

        // TODO moving this here might cause headaches
        AffinityStatusEffects.register();

        AutoRegistryContainer.register(AffinityBlocks.class, MOD_ID, true);
        AutoRegistryContainer.register(AffinityItems.class, MOD_ID, false);
        AutoRegistryContainer.register(AffinityEntities.class, MOD_ID, false);
        AffinityEntityAttributes.initialize();
        AutoRegistryContainer.register(AffinityParticleTypes.class, MOD_ID, false);
        AutoRegistryContainer.register(AffinityRecipeTypes.class, MOD_ID, true);
        AutoRegistryContainer.register(AffinityScreenHandlerTypes.class, MOD_ID, false);
        AutoRegistryContainer.register(AffinityEnchantmentEffectComponents.class, MOD_ID, false);

        IlliteracyEffectLogic.initialize();
        GravecallerEnchantmentLogic.initialize();
        BastionEnchantmentLogic.initialize();
        BerserkerEnchantmentLogic.initialize();
        HealthCurseEnchantmentLogic.initialize();

        FieldRegistrationHandler.processSimple(AffinitySoundEvents.class, false);
        FieldRegistrationHandler.processSimple(AffinityCriteria.class, false);

        AffinityNetwork.initialize();
        AffinityParticleSystems.initialize();
        AffinityPoiTypes.initialize();
        InquiryQuestions.initialize();

        LivingEntityHealthPredicate.register();
        AffinityCustomIngredients.initialize();

        EchoShardExtension.apply();
        AffinityWorldgen.initialize();

        Registry.register(Registries.LOOT_CONDITION_TYPE, Affinity.id("clump_direction"), ClumpDirectionLootCondition.TYPE);
        ResourceConditions.register(UnfinishedFeaturesResourceCondition.TYPE);

        AffinityStructures.register();

        BlockEntityType.SIGN.addSupportedBlock(AffinityBlocks.AZALEA_SIGN);
        BlockEntityType.SIGN.addSupportedBlock(AffinityBlocks.AZALEA_WALL_SIGN);

        BlockEntityType.HANGING_SIGN.addSupportedBlock(AffinityBlocks.AZALEA_HANGING_SIGN);
        BlockEntityType.HANGING_SIGN.addSupportedBlock(AffinityBlocks.AZALEA_WALL_HANGING_SIGN);

        AffinityItemGroup.group().initialize();

        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (!EntityType.WARDEN.getLootTableId().equals(key)) return;

            builder.pool(LootPool.builder()
                .with(ItemEntry.builder(AffinityItems.RESONANCE_CRYSTAL).apply(EnchantedCountIncreaseLootFunction.builder(registries, UniformLootNumberProvider.create(0, .75f))))
                .conditionally(EntityPropertiesLootCondition.builder(
                    LootContext.EntityTarget.ATTACKING_PLAYER, EntityPredicate.Builder.create()
                        .equipment(EntityEquipmentPredicate.Builder.create()
                            .mainhand(ItemPredicate.Builder.create().tag(TagKey.of(RegistryKeys.ITEM, Affinity.id("artifact_blades"))))
                            .build()))
                ));
        });

        AffinityCommands.register();

        if (!Owo.DEBUG) return;
        AffinityDebugCommands.register();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static String idPlain(String path) {
        return id(path).toString();
    }

    public static io.wispforest.affinity.AffinityConfig config() {
        return CONFIG;
    }

    public static boolean onClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static <T> ComponentType<T> component(String name, Endec<T> endec) {
        return Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            id(name),
            ComponentType.<T>builder()
                .codec(CodecUtils.toCodec(endec))
                .packetCodec(CodecUtils.toPacketCodec(endec))
                .build()
        );
    }

    public static ComponentType<Unit> unitComponent(String name) {
        return Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            id(name),
            ComponentType.<Unit>builder()
                .codec(Unit.CODEC)
                .packetCodec(PacketCodec.unit(Unit.INSTANCE))
                .build()
        );
    }

    public static <T> ComponentType<T> transientComponent(String name, Endec<T> endec) {
        return Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            id(name),
            ComponentType.<T>builder()
                .packetCodec(CodecUtils.toPacketCodec(endec))
                .build()
        );
    }
}
