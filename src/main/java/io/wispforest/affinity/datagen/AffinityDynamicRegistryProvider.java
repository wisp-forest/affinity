package io.wispforest.affinity.datagen;

import io.wispforest.affinity.worldgen.AffinityWorldgen;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class AffinityDynamicRegistryProvider extends FabricDynamicRegistryProvider {
    public AffinityDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        entries.add(AffinityWorldgen.AZALEA_TREE, registries.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE).getOrThrow(AffinityWorldgen.AZALEA_TREE).value());
        entries.add(AffinityWorldgen.OAK_AND_AZALEA_TREE, registries.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE).getOrThrow(AffinityWorldgen.OAK_AND_AZALEA_TREE).value());
        entries.add(AffinityWorldgen.WISP_FOREST_GRASS, registries.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE).getOrThrow(AffinityWorldgen.WISP_FOREST_GRASS).value());
        entries.add(AffinityWorldgen.WISP_FOREST_FLOWERS, registries.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE).getOrThrow(AffinityWorldgen.WISP_FOREST_FLOWERS).value());
        entries.add(AffinityWorldgen.FLOWER_WISP_FOREST, registries.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE).getOrThrow(AffinityWorldgen.FLOWER_WISP_FOREST).value());
        entries.add(AffinityWorldgen.CULTIVATION_STAFF_FLOWER_PATCH, registries.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE).getOrThrow(AffinityWorldgen.CULTIVATION_STAFF_FLOWER_PATCH).value());
        entries.add(AffinityWorldgen.CULTIVATION_STAFF_GRASS_PATCH, registries.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE).getOrThrow(AffinityWorldgen.CULTIVATION_STAFF_GRASS_PATCH).value());
        entries.add(AffinityWorldgen.CONFIGURED_CULTIVATION_STAFF_GRASS_PATCH, registries.getWrapperOrThrow(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(AffinityWorldgen.CONFIGURED_CULTIVATION_STAFF_GRASS_PATCH).value());
        entries.add(AffinityWorldgen.CONFIGURED_CULTIVATION_STAFF_FLOWER_PATCH, registries.getWrapperOrThrow(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(AffinityWorldgen.CONFIGURED_CULTIVATION_STAFF_FLOWER_PATCH).value());
        entries.add(AffinityWorldgen.CONFIGURED_OAK_AND_AZALEA_TREE, registries.getWrapperOrThrow(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(AffinityWorldgen.CONFIGURED_OAK_AND_AZALEA_TREE).value());

        entries.add(AffinityWorldgen.WISP_FOREST_KEY, registries.getWrapperOrThrow(RegistryKeys.BIOME).getOrThrow(AffinityWorldgen.WISP_FOREST_KEY).value());
    }

    @Override
    public String getName() {
        return "Affinity dynamic registries";
    }
}
