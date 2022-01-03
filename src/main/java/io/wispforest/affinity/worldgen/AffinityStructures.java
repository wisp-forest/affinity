package io.wispforest.affinity.worldgen;

import io.wispforest.affinity.Affinity;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.structure.PlainsVillageData;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class AffinityStructures {

    public static final StructureFeature<StructurePoolFeatureConfig> BIKESHED = new BikeshedFeature(StructurePoolFeatureConfig.CODEC);

    public static final ConfiguredStructureFeature<?, ?> CONFIGURED_BIKESHED = BIKESHED.configure(
            new StructurePoolFeatureConfig(() -> PlainsVillageData.STRUCTURE_POOLS, 0));

    public static void register() {
        FabricStructureBuilder.create(Affinity.id("bikeshed"), BIKESHED)
                .step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .defaultConfig(new StructureConfig(3, 1, 20598149))
                .adjustsSurface()
                .register();

        Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, Affinity.id("bikeshed"), CONFIGURED_BIKESHED);
    }

}
