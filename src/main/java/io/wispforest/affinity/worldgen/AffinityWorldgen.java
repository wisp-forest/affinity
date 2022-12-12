package io.wispforest.affinity.worldgen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.mixin.access.OverworldBiomeCreatorInvoker;
import io.wispforest.affinity.object.AffinityEntities;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import terrablender.api.Regions;
import terrablender.api.TerraBlenderApi;

// TODO pain 2.0
public class AffinityWorldgen {

    public static final RegistryKey<Biome> WISP_FOREST_KEY = RegistryKey.of(RegistryKeys.BIOME, Affinity.id("wisp_forest"));
//
//    public static final RegistryEntry<PlacedFeature> AZALEA_TREE = PlacedFeatures.register(Affinity.idPlain("azalea_tree"), TreeConfiguredFeatures.AZALEA_TREE, PlacedFeatures.wouldSurvive(Blocks.AZALEA));
//
//    public static final RegistryEntry<PlacedFeature> OAK_AND_AZALEA_TREE = PlacedFeatures.register(Affinity.idPlain("oak_and_azalea_tree"),
//            ConfiguredFeatures.register(Affinity.idPlain("oak_and_azalea_tree"), Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(
//                    new RandomFeatureEntry(AZALEA_TREE, 0.15F)),
//                    TreePlacedFeatures.OAK_BEES_0002)), VegetationPlacedFeatures.modifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));
//
//    public static final RegistryEntry<PlacedFeature> WISP_FOREST_GRASS = PlacedFeatures.register(Affinity.idPlain("wisp_forest_grass"),
//            VegetationConfiguredFeatures.PATCH_GRASS_JUNGLE, VegetationPlacedFeatures.modifiers(25));
//
//    public static final RegistryEntry<PlacedFeature> WISP_FOREST_FLOWERS = PlacedFeatures.register(
//            Affinity.idPlain("wisp_forest_flowers"),
//            VegetationConfiguredFeatures.FOREST_FLOWERS,
//            RarityFilterPlacementModifier.of(7),
//            SquarePlacementModifier.of(),
//            PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
//            CountPlacementModifier.of(ClampedIntProvider.create(UniformIntProvider.create(-1, 3), 0, 3)),
//            BiomePlacementModifier.of()
//    );
//
//    public static final RegistryEntry<PlacedFeature> FLOWER_WISP_FOREST = PlacedFeatures.register(
//            Affinity.idPlain("flower_wisp_forest"),
//            VegetationConfiguredFeatures.FLOWER_FLOWER_FOREST,
//            CountPlacementModifier.of(3),
//            RarityFilterPlacementModifier.of(2),
//            SquarePlacementModifier.of(),
//            PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
//            BiomePlacementModifier.of()
//    );

    public static void initialize() {
//        Registry.register(BuiltinRegistries.BIOME, WISP_FOREST_KEY, makeWispForest());
    }

    private static Biome makeWispForest() {
        var generation = new GenerationSettings.Builder();

//        DefaultBiomeFeatures.addLandCarvers(generation);
//        DefaultBiomeFeatures.addAmethystGeodes(generation);
//        DefaultBiomeFeatures.addDungeons(generation);
//        DefaultBiomeFeatures.addMineables(generation);
//        DefaultBiomeFeatures.addSprings(generation);
//        DefaultBiomeFeatures.addFrozenTopLayer(generation);
//        DefaultBiomeFeatures.addForestFlowers(generation);
//        DefaultBiomeFeatures.addDefaultOres(generation);
//        DefaultBiomeFeatures.addDefaultDisks(generation);
//        DefaultBiomeFeatures.addDefaultFlowers(generation);
//        DefaultBiomeFeatures.addForestGrass(generation);
//        DefaultBiomeFeatures.addDefaultMushrooms(generation);
//        DefaultBiomeFeatures.addDefaultVegetation(generation);

//        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, WISP_FOREST_GRASS);
//        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, WISP_FOREST_FLOWERS);
//        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, FLOWER_WISP_FOREST);
//        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, OAK_AND_AZALEA_TREE);

        var effects = new BiomeEffects.Builder()
                .grassColor(0x5AA469)
                .fogColor(OverworldBiomeCreatorInvoker.affinity$getSkyColor(.7f))
                .waterColor(0x94B3FD)
                .waterFogColor(0x94DAFF)
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
