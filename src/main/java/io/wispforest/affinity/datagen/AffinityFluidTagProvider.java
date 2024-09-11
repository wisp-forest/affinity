package io.wispforest.affinity.datagen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class AffinityFluidTagProvider extends FabricTagProvider.FluidTagProvider {

    public static final TagKey<Fluid> ARCANE_FADE = TagKey.of(RegistryKeys.FLUID, Identifier.of("c","arcane_fade"));

    public AffinityFluidTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        var arcaneFadeInternal = TagKey.of(RegistryKeys.FLUID, Affinity.id("arcane_fade"));

        this.getOrCreateTagBuilder(arcaneFadeInternal).add(AffinityBlocks.Fluids.ARCANE_FADE, AffinityBlocks.Fluids.ARCANE_FADE_FLOWING);
        this.getOrCreateTagBuilder(ARCANE_FADE).addTag(arcaneFadeInternal);
    }
}
