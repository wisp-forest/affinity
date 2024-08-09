package io.wispforest.affinity.datagen;

import io.wispforest.affinity.object.AffinityEnchantments;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EnchantmentTags;

import java.util.concurrent.CompletableFuture;

public class AffinityEnchantmentTagProvider extends FabricTagProvider.EnchantmentTagProvider {
    public AffinityEnchantmentTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(EnchantmentTags.CURSE)
            .add(AffinityEnchantments.CURSE_OF_ILLITERACY);
//            .add(AffinityEnchantments.CURSE_OF_HEALTH);
    }
}
