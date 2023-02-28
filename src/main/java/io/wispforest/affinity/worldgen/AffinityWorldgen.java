package io.wispforest.affinity.worldgen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.mixin.access.OverworldBiomeCreatorInvoker;
import io.wispforest.affinity.object.AffinityEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.intprovider.ClampedIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.NoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;
import terrablender.api.Regions;
import terrablender.api.TerraBlenderApi;

import java.util.List;

public class AffinityWorldgen {

    public static final RegistryKey<Biome> WISP_FOREST_KEY = RegistryKey.of(RegistryKeys.BIOME, Affinity.id("wisp_forest"));

    public static final RegistryKey<PlacedFeature> AZALEA_TREE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Affinity.id("azalea_tree"));

    public static final RegistryKey<ConfiguredFeature<?, ?>> CONFIGURED_OAK_AND_AZALEA_TREE = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Affinity.id("oak_and_azalea_tree"));
    public static final RegistryKey<PlacedFeature> OAK_AND_AZALEA_TREE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Affinity.id("oak_and_azalea_tree"));

    public static final RegistryKey<PlacedFeature> WISP_FOREST_GRASS = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Affinity.id("wisp_forest_grass"));
    public static final RegistryKey<PlacedFeature> WISP_FOREST_FLOWERS = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Affinity.id("wisp_forest_flowers"));
    public static final RegistryKey<PlacedFeature> FLOWER_WISP_FOREST = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Affinity.id("flower_wisp_forest"));

    public static final RegistryKey<ConfiguredFeature<?, ?>> CONFIGURED_CULTIVATION_STAFF_FLOWER_PATCH = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Affinity.id("cultivation_staff_flower_patch"));
    public static final RegistryKey<ConfiguredFeature<?, ?>> CONFIGURED_CULTIVATION_STAFF_GRASS_PATCH = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Affinity.id("cultivation_staff_grass_patch"));
    public static final RegistryKey<PlacedFeature> CULTIVATION_STAFF_FLOWER_PATCH = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Affinity.id("cultivation_staff_flower_patch"));
    public static final RegistryKey<PlacedFeature> CULTIVATION_STAFF_GRASS_PATCH = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Affinity.id("cultivation_staff_grass_patch"));

    public static void bootstrapAzaleaTree(Registerable<PlacedFeature> featureRegisterable) {
        var featureLookup = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        PlacedFeatures.register(featureRegisterable, AZALEA_TREE, featureLookup.getOrThrow(TreeConfiguredFeatures.AZALEA_TREE), PlacedFeatures.wouldSurvive(Blocks.AZALEA));
    }

    public static void bootstrapConfiguredFeatures(Registerable<ConfiguredFeature<?, ?>> featureRegisterable) {
        var featureLookup = featureRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        ConfiguredFeatures.register(
                featureRegisterable,
                CONFIGURED_OAK_AND_AZALEA_TREE,
                Feature.RANDOM_SELECTOR,
                new RandomFeatureConfig(
                        List.of(new RandomFeatureEntry(featureLookup.getOrThrow(AZALEA_TREE), 0.15F)),
                        featureLookup.getOrThrow(TreePlacedFeatures.OAK_BEES_0002)
                )
        );

        ConfiguredFeatures.register(
                featureRegisterable,
                CONFIGURED_CULTIVATION_STAFF_GRASS_PATCH,
                Feature.RANDOM_PATCH,
                new RandomPatchFeatureConfig(
                        100, 7, 3,
                        PlacedFeatures.createEntry(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(DataPool.<BlockState>builder()
                                        .add(Blocks.GRASS.getDefaultState(), 4)
                                        .add(Blocks.TALL_GRASS.getDefaultState(), 1)
                                        .add(Blocks.FERN.getDefaultState(), 2)
                                ))
                        )
                )
        );

        ConfiguredFeatures.register(
                featureRegisterable,
                CONFIGURED_CULTIVATION_STAFF_FLOWER_PATCH,
                Feature.FLOWER,
                new RandomPatchFeatureConfig(
                        80, 6, 2,
                        PlacedFeatures.createEntry(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockFeatureConfig(new NoiseBlockStateProvider(
                                        2056, new DoublePerlinNoiseSampler.NoiseParameters(0, 1.0), 5f,
                                        List.of(
                                                Blocks.DANDELION.getDefaultState(), Blocks.POPPY.getDefaultState(),
                                                Blocks.ALLIUM.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(),
                                                Blocks.RED_TULIP.getDefaultState(), Blocks.ORANGE_TULIP.getDefaultState(),
                                                Blocks.WHITE_TULIP.getDefaultState(), Blocks.PINK_TULIP.getDefaultState(),
                                                Blocks.OXEYE_DAISY.getDefaultState(), Blocks.CORNFLOWER.getDefaultState(),
                                                Blocks.LILY_OF_THE_VALLEY.getDefaultState(), Blocks.FERN.getDefaultState()
                                        )
                                ))
                        )
                )
        );
    }

    public static void bootstrapPlacedFeatures(Registerable<PlacedFeature> featureRegisterable) {
        var featureLookup = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        PlacedFeatures.register(
                featureRegisterable,
                OAK_AND_AZALEA_TREE,
                featureLookup.getOrThrow(CONFIGURED_OAK_AND_AZALEA_TREE),
                VegetationPlacedFeatures.modifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1))
        );

        PlacedFeatures.register(
                featureRegisterable,
                WISP_FOREST_GRASS,
                featureLookup.getOrThrow(VegetationConfiguredFeatures.PATCH_GRASS_JUNGLE),
                VegetationPlacedFeatures.modifiers(25)
        );

        PlacedFeatures.register(
                featureRegisterable,
                FLOWER_WISP_FOREST,
                featureLookup.getOrThrow(VegetationConfiguredFeatures.FLOWER_FLOWER_FOREST),
                CountPlacementModifier.of(3),
                RarityFilterPlacementModifier.of(2),
                SquarePlacementModifier.of(),
                PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                BiomePlacementModifier.of()
        );

        PlacedFeatures.register(
                featureRegisterable,
                WISP_FOREST_FLOWERS,
                featureLookup.getOrThrow(VegetationConfiguredFeatures.FOREST_FLOWERS),
                RarityFilterPlacementModifier.of(7),
                SquarePlacementModifier.of(),
                PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                CountPlacementModifier.of(ClampedIntProvider.create(UniformIntProvider.create(-1, 3), 0, 3)),
                BiomePlacementModifier.of()
        );

        PlacedFeatures.register(
                featureRegisterable,
                CULTIVATION_STAFF_FLOWER_PATCH,
                featureLookup.getOrThrow(CONFIGURED_CULTIVATION_STAFF_FLOWER_PATCH)
        );

        PlacedFeatures.register(
                featureRegisterable,
                CULTIVATION_STAFF_GRASS_PATCH,
                featureLookup.getOrThrow(CONFIGURED_CULTIVATION_STAFF_GRASS_PATCH)
        );
    }

    public static void bootstrapBiomes(Registerable<Biome> biomeRegisterable) {
        biomeRegisterable.register(
                WISP_FOREST_KEY,
                makeWispForest(
                        biomeRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE),
                        biomeRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER)
                )
        );
    }

    private static Biome makeWispForest(RegistryEntryLookup<PlacedFeature> features, RegistryEntryLookup<ConfiguredCarver<?>> carvers) {
        var generation = new GenerationSettings.LookupBackedBuilder(features, carvers);

        DefaultBiomeFeatures.addLandCarvers(generation);
        DefaultBiomeFeatures.addAmethystGeodes(generation);
        DefaultBiomeFeatures.addDungeons(generation);
        DefaultBiomeFeatures.addMineables(generation);
        DefaultBiomeFeatures.addSprings(generation);
        DefaultBiomeFeatures.addFrozenTopLayer(generation);
        DefaultBiomeFeatures.addDefaultOres(generation);
        DefaultBiomeFeatures.addDefaultDisks(generation);
        DefaultBiomeFeatures.addDefaultFlowers(generation);
        DefaultBiomeFeatures.addDefaultMushrooms(generation);
        DefaultBiomeFeatures.addDefaultVegetation(generation);

        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, OAK_AND_AZALEA_TREE);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, WISP_FOREST_GRASS);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, WISP_FOREST_FLOWERS);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, FLOWER_WISP_FOREST);

        var effects = new BiomeEffects.Builder()
                .grassColor(0x5AA469)
                .waterColor(0x94B3FD)
                .waterFogColor(0x94DAFF)
                .fogColor(OverworldBiomeCreatorInvoker.affinity$getSkyColor(.7f))
                .skyColor(OverworldBiomeCreatorInvoker.affinity$getSkyColor(.7f))
                .moodSound(BiomeMoodSound.CAVE)
                .music(null)
                .build();

        var spawnSettings = new SpawnSettings.Builder();
        spawnSettings.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(AffinityEntities.INERT_WISP, 10, 2, 5));
        spawnSettings.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(AffinityEntities.WISE_WISP, 5, 2, 5));
        spawnSettings.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(AffinityEntities.VICIOUS_WISP, 2, 5, 15));
        DefaultBiomeFeatures.addFarmAnimals(spawnSettings);

        return new Biome.Builder()
                .precipitation(Biome.Precipitation.RAIN)
                .temperature(.7f)
                .downfall(.4f)
                .effects(effects)
                .spawnSettings(spawnSettings.build())
                .generationSettings(generation.build())
                .build();
    }

    public static class TerraBlenderHook implements TerraBlenderApi {
        @Override
        public void onTerraBlenderInitialized() {
            Regions.register(new AffinityBiomeRegion());
        }
    }
}
