package io.wispforest.affinity.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AffinityDataGeneratorEntrypoint implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(AffinityBlockStateDefinitionProvider::new);
        pack.addProvider(AffinityBlockTagProvider::new);
        pack.addProvider(AffinityEntityLootTableProvider::new);
        pack.addProvider(AffinityRecipesProvider::new);
    }
}
