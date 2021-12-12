package io.wispforest.affinity;

import io.wispforest.affinity.item.AffinityItemGroup;
import io.wispforest.affinity.mixin.TreeConfigureFeaturesAccessor;
import io.wispforest.affinity.registries.AffinityBlocks;
import io.wispforest.affinity.registries.AffinityItems;
import io.wispforest.affinity.registries.AffinityStatusEffects;
import io.wispforest.affinity.util.aethumflux.AethumNetworkMember;
import io.wispforest.affinity.util.aethumflux.AethumNetworkNode;
import io.wispforest.affinity.util.recipe.PotionMixingRecipe;
import io.wispforest.affinity.util.recipe.PotionMixingRecipeSerializer;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.RandomSpreadFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.trunk.BendingTrunkPlacer;

public class Affinity implements ModInitializer {

    public static final String MOD_ID = "affinity";
    public static final OwoItemGroup AFFINITY_GROUP = new AffinityItemGroup(id("affinity"));

    public static final BlockApiLookup<AethumNetworkMember, Void> AETHUM_MEMBER = BlockApiLookup.get(id("aethum_member"), AethumNetworkMember.class, Void.class);
    public static final BlockApiLookup<AethumNetworkNode, Void> AETHUM_NODE = BlockApiLookup.get(id("aethum_node"), AethumNetworkNode.class, Void.class);

    public static final ConfiguredFeature<?, ?> REAL_AZALEA_TREE = Feature.TREE.configure((new TreeFeatureConfig.Builder(BlockStateProvider.of(AffinityBlocks.AZALEA_LOG), new BendingTrunkPlacer(4, 2, 0, 3, UniformIntProvider.create(1, 2)), new WeightedBlockStateProvider(DataPool.<BlockState>builder().add(Blocks.AZALEA_LEAVES.getDefaultState(), 3).add(Blocks.FLOWERING_AZALEA_LEAVES.getDefaultState(), 1)), new RandomSpreadFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), ConstantIntProvider.create(2), 50), new TwoLayersFeatureSize(1, 0, 1))).dirtProvider(BlockStateProvider.of(Blocks.ROOTED_DIRT)).forceDirt().build());

    @Override
    public void onInitialize() {
        FieldRegistrationHandler.register(AffinityBlocks.class, MOD_ID, false);
        FieldRegistrationHandler.register(AffinityBlocks.Entities.class, MOD_ID, false);

        FieldRegistrationHandler.register(AffinityItems.class, MOD_ID, false);

        int azaleaId = BuiltinRegistries.CONFIGURED_FEATURE.getRawId(TreeConfiguredFeatures.AZALEA_TREE);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, azaleaId, "azalea_tree", REAL_AZALEA_TREE);
        TreeConfigureFeaturesAccessor.setAzaleaTree(REAL_AZALEA_TREE);

        AffinityStatusEffects.register();

        Registry.register(Registry.RECIPE_TYPE, PotionMixingRecipe.Type.ID, PotionMixingRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, PotionMixingRecipeSerializer.ID, PotionMixingRecipeSerializer.INSTANCE);

        AFFINITY_GROUP.initialize();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

}
