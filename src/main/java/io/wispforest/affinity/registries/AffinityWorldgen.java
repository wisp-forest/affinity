package io.wispforest.affinity.registries;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.block.Blocks;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.ClampedIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.BiomePlacementModifier;
import net.minecraft.world.gen.decorator.CountPlacementModifier;
import net.minecraft.world.gen.decorator.RarityFilterPlacementModifier;
import net.minecraft.world.gen.decorator.SquarePlacementModifier;
import net.minecraft.world.gen.feature.*;

import java.util.List;

public class AffinityWorldgen implements AutoRegistryContainer<PlacedFeature> {

//    private final RegistryKey<Biome>[][] OCEAN_BIOMES = new RegistryKey[][]{
//            {BiomeKeys.DEEP_FROZEN_OCEAN, BiomeKeys.DEEP_COLD_OCEAN, BiomeKeys.DEEP_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN, BiomeKeys.WARM_OCEAN},
//            {BiomeKeys.FROZEN_OCEAN, BiomeKeys.COLD_OCEAN, BiomeKeys.OCEAN, BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.WARM_OCEAN}};
//    private final RegistryKey<Biome>[][] COMMON_BIOMES = new RegistryKey[][]{
//            {BiomeKeys.SNOWY_PLAINS, BiomeKeys.SNOWY_PLAINS, BiomeKeys.SNOWY_PLAINS, BiomeKeys.SNOWY_TAIGA, BiomeKeys.TAIGA},
//            {BiomeKeys.PLAINS, BiomeKeys.PLAINS, BiomeKeys.FOREST, BiomeKeys.TAIGA, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA},
//            {BiomeKeys.FLOWER_FOREST, BiomeKeys.PLAINS, BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.DARK_FOREST},
//            {BiomeKeys.SAVANNA, BiomeKeys.SAVANNA, BiomeKeys.FOREST, BiomeKeys.JUNGLE, BiomeKeys.JUNGLE},
//            {BiomeKeys.DESERT, BiomeKeys.DESERT, BiomeKeys.DESERT, BiomeKeys.DESERT, BiomeKeys.DESERT}};
//    private final RegistryKey<Biome>[][] UNCOMMON_BIOMES = new RegistryKey[][]{
//            {BiomeKeys.ICE_SPIKES, null, BiomeKeys.SNOWY_TAIGA, null, null},
//            {null, null, null, null, BiomeKeys.OLD_GROWTH_PINE_TAIGA},
//            {BiomeKeys.SUNFLOWER_PLAINS, null, null, BiomeKeys.OLD_GROWTH_BIRCH_FOREST, null},
//            {null, null, BiomeKeys.PLAINS, BiomeKeys.SPARSE_JUNGLE, BiomeKeys.BAMBOO_JUNGLE},
//            {null, null, null, null, null}};
//    private final RegistryKey<Biome>[][] NEAR_MOUNTAIN_BIOMES = new RegistryKey[][]{
//            {BiomeKeys.SNOWY_PLAINS, BiomeKeys.SNOWY_PLAINS, BiomeKeys.SNOWY_PLAINS, BiomeKeys.SNOWY_TAIGA, BiomeKeys.SNOWY_TAIGA},
//            {BiomeKeys.MEADOW, BiomeKeys.MEADOW, BiomeKeys.FOREST, BiomeKeys.TAIGA, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA}, {BiomeKeys.MEADOW, BiomeKeys.MEADOW, BiomeKeys.MEADOW, BiomeKeys.MEADOW, BiomeKeys.DARK_FOREST}, {BiomeKeys.SAVANNA_PLATEAU, BiomeKeys.SAVANNA_PLATEAU, BiomeKeys.FOREST, BiomeKeys.FOREST, BiomeKeys.JUNGLE}, {BiomeKeys.BADLANDS, BiomeKeys.BADLANDS, BiomeKeys.BADLANDS, BiomeKeys.WOODED_BADLANDS, BiomeKeys.WOODED_BADLANDS}};
//    private final RegistryKey<Biome>[][] SPECIAL_NEAR_MOUNTAIN_BIOMES = new RegistryKey[][]{
//            {BiomeKeys.ICE_SPIKES, null, null, null, null}, {null, null, BiomeKeys.MEADOW, BiomeKeys.MEADOW, BiomeKeys.OLD_GROWTH_PINE_TAIGA},
//            {null, null, BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, null}, {null, null, null, null, null}, {BiomeKeys.ERODED_BADLANDS, BiomeKeys.ERODED_BADLANDS, null, null, null}};
//    private final RegistryKey<Biome>[][] HILL_BIOMES = new RegistryKey[][]{
//            {BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, BiomeKeys.WINDSWEPT_HILLS, BiomeKeys.WINDSWEPT_FOREST, BiomeKeys.WINDSWEPT_FOREST},
//            {BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, BiomeKeys.WINDSWEPT_HILLS, BiomeKeys.WINDSWEPT_FOREST, BiomeKeys.WINDSWEPT_FOREST}, {BiomeKeys.WINDSWEPT_HILLS, BiomeKeys.WINDSWEPT_HILLS, BiomeKeys.WINDSWEPT_HILLS, BiomeKeys.WINDSWEPT_FOREST, BiomeKeys.WINDSWEPT_FOREST}, {null, null, null, null, null}, {null, null, null, null, null}};

    public static final RegistryKey<Biome> WISP_FOREST_KEY = RegistryKey.of(Registry.BIOME_KEY, Affinity.id("wisp_forest"));
    public static Biome WISP_FOREST;

    public static final PlacedFeature AZALEA_TREE = TreeConfiguredFeatures.AZALEA_TREE
            .withWouldSurviveFilter(Blocks.AZALEA);

    public static final PlacedFeature OAK_AND_AZALEA_TREE = Feature.RANDOM_SELECTOR.configure(
                    new RandomFeatureConfig(List.of(
                            new RandomFeatureEntry(AZALEA_TREE, 0.15F)),
                            TreePlacedFeatures.OAK_BEES_0002))
            .withPlacement(VegetationPlacedFeatures.modifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));

    public static final PlacedFeature WISP_FOREST_GRASS = VegetationConfiguredFeatures.PATCH_GRASS_JUNGLE.withPlacement(VegetationPlacedFeatures.modifiers(25));
    public static final PlacedFeature FLOWER_WISP_FOREST = VegetationConfiguredFeatures.FLOWER_FLOWER_FOREST.withPlacement(CountPlacementModifier.of(3), RarityFilterPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
    public static final PlacedFeature WISP_FOREST_FLOWERS = VegetationConfiguredFeatures.FOREST_FLOWERS.withPlacement(RarityFilterPlacementModifier.of(7), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, CountPlacementModifier.of(ClampedIntProvider.create(UniformIntProvider.create(-1, 3), 0, 3)), BiomePlacementModifier.of());

    public static void registerBiomes() {
        WISP_FOREST = Registry.register(BuiltinRegistries.BIOME, WISP_FOREST_KEY, makeWispForest());
    }

    private static Biome makeWispForest() {
        var generation = new GenerationSettings.Builder();

        DefaultBiomeFeatures.addLandCarvers(generation);
        DefaultBiomeFeatures.addAmethystGeodes(generation);
        DefaultBiomeFeatures.addDungeons(generation);
        DefaultBiomeFeatures.addMineables(generation);
        DefaultBiomeFeatures.addSprings(generation);
        DefaultBiomeFeatures.addFrozenTopLayer(generation);
        DefaultBiomeFeatures.addForestFlowers(generation);
        DefaultBiomeFeatures.addDefaultOres(generation);
        DefaultBiomeFeatures.addDefaultDisks(generation);
        DefaultBiomeFeatures.addDefaultFlowers(generation);
        DefaultBiomeFeatures.addForestGrass(generation);
        DefaultBiomeFeatures.addDefaultMushrooms(generation);
        DefaultBiomeFeatures.addDefaultVegetation(generation);

        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, WISP_FOREST_GRASS);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, FLOWER_WISP_FOREST);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, WISP_FOREST_FLOWERS);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, OAK_AND_AZALEA_TREE);

        var effects = new BiomeEffects.Builder()
                .grassColor(0x5AA469)
                .fogColor(getSkyColor(.7f))
                .waterColor(0x94B3FD)
                .waterFogColor(0x94DAFF)
                .skyColor(getSkyColor(.7f))
                .moodSound(BiomeMoodSound.CAVE)
                .music(null)
                .build();

        var spawnSettings = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(spawnSettings);

        return new Biome.Builder()
                .precipitation(Biome.Precipitation.RAIN)
                .category(Biome.Category.FOREST)
                .temperature(.7f)
                .downfall(.4f)
                .effects(effects)
                .spawnSettings(spawnSettings.build())
                .generationSettings(generation.build())
                .build();
    }

    private static int getSkyColor(float temperature) {
        float f = temperature / 3.0F;
        f = MathHelper.clamp(f, -1.0F, 1.0F);
        return MathHelper.hsvToRgb(0.62222224F - f * 0.05F, 0.5F + f * 0.1F, 1.0F);
    }

    @Override
    public Registry<PlacedFeature> getRegistry() {
        return BuiltinRegistries.PLACED_FEATURE;
    }

    @Override
    public Class<PlacedFeature> getTargetFieldType() {
        return PlacedFeature.class;
    }
}
