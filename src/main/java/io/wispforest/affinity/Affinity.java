package io.wispforest.affinity;

import com.google.common.collect.ImmutableSet;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.item.AffinityItemGroup;
import io.wispforest.affinity.mixin.access.BlockEntityTypeAccessor;
import io.wispforest.affinity.mixin.access.SignTypeInvoker;
import io.wispforest.affinity.mixin.access.TreeFeatureConfigAccessor;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.registries.AffinityBlocks;
import io.wispforest.affinity.registries.AffinityEntities;
import io.wispforest.affinity.registries.AffinityItems;
import io.wispforest.affinity.registries.AffinityStatusEffects;
import io.wispforest.affinity.util.recipe.PotionMixingRecipe;
import io.wispforest.affinity.util.recipe.PotionMixingRecipeSerializer;
import io.wispforest.affinity.worldgen.AffinityStructures;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Affinity implements ModInitializer {

    public static final String MOD_ID = "affinity";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final OwoItemGroup AFFINITY_GROUP = new AffinityItemGroup(id("affinity"));

    public static final BlockApiLookup<AethumNetworkMember, Void> AETHUM_MEMBER = BlockApiLookup.get(id("aethum_member"), AethumNetworkMember.class, Void.class);
    public static final BlockApiLookup<AethumNetworkNode, Void> AETHUM_NODE = BlockApiLookup.get(id("aethum_node"), AethumNetworkNode.class, Void.class);

    @Override
    public void onInitialize() {
        FieldRegistrationHandler.register(AffinityBlocks.class, MOD_ID, false);
        FieldRegistrationHandler.register(AffinityBlocks.Entities.class, MOD_ID, false);

        FieldRegistrationHandler.register(AffinityItems.class, MOD_ID, false);

        FieldRegistrationHandler.register(AffinityEntities.class, MOD_ID, false);

        AffinityStatusEffects.register();
        AffinityNetwork.initialize();

        //noinspection ConstantConditions
        var azaleaConfig = (TreeFeatureConfigAccessor) BuiltinRegistries.CONFIGURED_FEATURE.get(new Identifier("azalea_tree")).config;
        azaleaConfig.setAzaleaTree(BlockStateProvider.of(AffinityBlocks.AZALEA_LOG));

        AffinityStructures.register();

        FieldRegistrationHandler.register(AffinityWorldgen.class, MOD_ID, false);
        AffinityWorldgen.registerBiomes();

        // Inject our sign block into the BE type
        var signBlocks = ((BlockEntityTypeAccessor) BlockEntityType.SIGN).affinity$getBlocks();
        signBlocks = ImmutableSet.<Block>builder().addAll(signBlocks).add(AffinityBlocks.AZALEA_SIGN, AffinityBlocks.AZALEA_WALL_SIGN).build();
        ((BlockEntityTypeAccessor) BlockEntityType.SIGN).affinity$setBlocks(signBlocks);

        SignTypeInvoker.affinity$invokeRegister(AffinityBlocks.AZALEA_SIGN_TYPE);

        Registry.register(Registry.RECIPE_TYPE, PotionMixingRecipe.Type.ID, PotionMixingRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, PotionMixingRecipeSerializer.ID, PotionMixingRecipeSerializer.INSTANCE);

        AFFINITY_GROUP.initialize();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

}
