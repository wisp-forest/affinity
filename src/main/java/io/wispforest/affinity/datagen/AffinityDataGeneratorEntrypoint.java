package io.wispforest.affinity.datagen;

import io.wispforest.affinity.object.AffinityEnchantments;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class AffinityDataGeneratorEntrypoint implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(AffinityBlockStateModelProvider::new);
        pack.addProvider(AffinityItemModelProvider::new);
        pack.addProvider(AffinityEntityLootTableProvider::new);
        pack.addProvider(AffinityBlockLootTableProvider::new);
        pack.addProvider(AffinityRecipesProvider::new);
        pack.addProvider(AffinityDynamicRegistryProvider::new);

        var blockTagProvider = pack.addProvider(AffinityBlockTagProvider::new);
        pack.addProvider((output, registries) -> new AffinityItemTagProvider(output, registries, blockTagProvider));
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, AffinityWorldgen::bootstrapAzaleaTree);
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, AffinityWorldgen::bootstrapConfiguredFeatures);
        registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, AffinityWorldgen::bootstrapPlacedFeatures);
        registryBuilder.addRegistry(RegistryKeys.BIOME, AffinityWorldgen::bootstrapBiomes);
        registryBuilder.addRegistry(RegistryKeys.ENCHANTMENT, AffinityEnchantments::bootstrap);
    }
}
