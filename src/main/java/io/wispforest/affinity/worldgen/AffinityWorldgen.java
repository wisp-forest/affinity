package io.wispforest.affinity.worldgen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityEntities;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnGroup;
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

    public static final RegistryKey<Biome> WISP_FOREST_KEY = RegistryKey.of(Registry.BIOME_KEY, Affinity.id("wisp_forest"));

    public static final PlacedFeature AZALEA_TREE = TreeConfiguredFeatures.AZALEA_TREE.withWouldSurviveFilter(Blocks.AZALEA);

    public static final PlacedFeature OAK_AND_AZALEA_TREE = Feature.RANDOM_SELECTOR.configure(
                    new RandomFeatureConfig(List.of(
                            new RandomFeatureEntry(AZALEA_TREE, 0.15F)),
                            TreePlacedFeatures.OAK_BEES_0002))
            .withPlacement(VegetationPlacedFeatures.modifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));

    public static final PlacedFeature WISP_FOREST_GRASS = VegetationConfiguredFeatures.PATCH_GRASS_JUNGLE.withPlacement(VegetationPlacedFeatures.modifiers(25));
    public static final PlacedFeature FLOWER_WISP_FOREST = VegetationConfiguredFeatures.FLOWER_FLOWER_FOREST.withPlacement(CountPlacementModifier.of(3), RarityFilterPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
    public static final PlacedFeature WISP_FOREST_FLOWERS = VegetationConfiguredFeatures.FOREST_FLOWERS.withPlacement(RarityFilterPlacementModifier.of(7), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, CountPlacementModifier.of(ClampedIntProvider.create(UniformIntProvider.create(-1, 3), 0, 3)), BiomePlacementModifier.of());

    public static void registerBiomes() {
        Registry.register(BuiltinRegistries.BIOME, WISP_FOREST_KEY, makeWispForest());
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
//        generation.feature()

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
        spawnSettings.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(AffinityEntities.INERT_WISP, 10, 2, 5));
        spawnSettings.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(AffinityEntities.VICIOUS_WISP, 2, 5, 15));
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
