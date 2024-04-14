package io.wispforest.affinity;

import com.google.common.collect.ImmutableSet;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.item.AffinityItemGroup;
import io.wispforest.affinity.item.EchoShardExtension;
import io.wispforest.affinity.misc.AffinityDebugCommands;
import io.wispforest.affinity.misc.ClumpDirectionLootCondition;
import io.wispforest.affinity.misc.InquiryQuestions;
import io.wispforest.affinity.mixin.access.BlockEntityTypeAccessor;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.*;
import io.wispforest.affinity.worldgen.AffinityStructures;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import io.wispforest.owo.Owo;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootingEnchantLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Affinity implements ModInitializer {

    public static final String MOD_ID = "affinity";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final io.wispforest.affinity.AffinityConfig CONFIG = io.wispforest.affinity.AffinityConfig.createAndLoad();

    public static final Color AETHUM_FLUX_COLOR = Color.ofRgb(0x6A67CE);

    public static final BlockApiLookup<AethumNetworkMember, Void> AETHUM_MEMBER = BlockApiLookup.get(id("aethum_member"), AethumNetworkMember.class, Void.class);
    public static final BlockApiLookup<AethumNetworkNode, Void> AETHUM_NODE = BlockApiLookup.get(id("aethum_node"), AethumNetworkNode.class, Void.class);

    @Override
    public void onInitialize() {
        AffinityItemGroup.register();

        AutoRegistryContainer.register(AffinityBlocks.class, MOD_ID, true);
        AutoRegistryContainer.register(AffinityItems.class, MOD_ID, false);
        AutoRegistryContainer.register(AffinityEnchantments.class, MOD_ID, false);
        AutoRegistryContainer.register(AffinityEntities.class, MOD_ID, false);
        AutoRegistryContainer.register(AffinityEntityAttributes.class, MOD_ID, false);
        AutoRegistryContainer.register(AffinityParticleTypes.class, MOD_ID, false);
        AutoRegistryContainer.register(AffinityRecipeTypes.class, MOD_ID, true);
        AutoRegistryContainer.register(AffinityScreenHandlerTypes.class, MOD_ID, false);

        FieldRegistrationHandler.processSimple(AffinitySoundEvents.class, false);
        FieldRegistrationHandler.processSimple(AffinityCriteria.class, false);
        FieldRegistrationHandler.processSimple(AffinityIngredients.class, false);

        AffinityStatusEffects.register();
        AffinityNetwork.initialize();
        AffinityParticleSystems.initialize();
        AffinityPoiTypes.initialize();
        InquiryQuestions.initialize();

        EchoShardExtension.apply();
        AffinityWorldgen.initialize();

        Registry.register(Registries.LOOT_CONDITION_TYPE, Affinity.id("clump_direction"), ClumpDirectionLootCondition.TYPE);

        AffinityStructures.register();

        // Inject our sign block into the BE type
        var signBlocks = ((BlockEntityTypeAccessor) BlockEntityType.SIGN).affinity$getBlocks();
        signBlocks = ImmutableSet.<Block>builder().addAll(signBlocks).add(AffinityBlocks.AZALEA_SIGN, AffinityBlocks.AZALEA_WALL_SIGN).build();
        ((BlockEntityTypeAccessor) BlockEntityType.SIGN).affinity$setBlocks(signBlocks);

        var hangingSignBlocks = ((BlockEntityTypeAccessor) BlockEntityType.HANGING_SIGN).affinity$getBlocks();
        hangingSignBlocks = ImmutableSet.<Block>builder().addAll(hangingSignBlocks).add(AffinityBlocks.AZALEA_HANGING_SIGN, AffinityBlocks.AZALEA_WALL_HANGING_SIGN).build();
        ((BlockEntityTypeAccessor) BlockEntityType.HANGING_SIGN).affinity$setBlocks(hangingSignBlocks);

        AffinityItemGroup.group().initialize();

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, builder, source) -> {
            if (!EntityType.WARDEN.getLootTableId().equals(id)) return;
            builder.pool(LootPool.builder()
                    .with(ItemEntry.builder(AffinityItems.RESONANCE_CRYSTAL).apply(LootingEnchantLootFunction.builder(UniformLootNumberProvider.create(0, .75f))))
                    .conditionally(EntityPropertiesLootCondition.builder(
                            LootContext.EntityTarget.KILLER_PLAYER, EntityPredicate.Builder.create()
                                    .equipment(EntityEquipmentPredicate.Builder.create()
                                            .mainhand(ItemPredicate.Builder.create().tag(TagKey.of(RegistryKeys.ITEM, Affinity.id("artifact_blades"))))
                                            .build()))
                    ));
        });

        if (!Owo.DEBUG) return;
        AffinityDebugCommands.register();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static String idPlain(String path) {
        return id(path).toString();
    }

    public static boolean onClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
}
