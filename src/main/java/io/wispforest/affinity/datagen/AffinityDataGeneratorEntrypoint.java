package io.wispforest.affinity.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AffinityDataGeneratorEntrypoint implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(AffinityBlockStateDefinitionProvider::new);
        fabricDataGenerator.addProvider(AffinityBlockTagProvider::new);
        fabricDataGenerator.addProvider(AffinityEntityLootTableProvider::new);
    }
}
