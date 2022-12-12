package io.wispforest.affinity.worldgen;

import com.mojang.datafixers.util.Pair;
import io.wispforest.affinity.Affinity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class AffinityBiomeRegion extends Region {

    public AffinityBiomeRegion() {
        super(Affinity.id("biome_region"), RegionType.OVERWORLD, 2);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> mapper) {
        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
            builder.replaceBiome(BiomeKeys.FOREST, AffinityWorldgen.WISP_FOREST_KEY);
        });
    }
}
