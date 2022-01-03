package io.wispforest.affinity.mixin;

import io.wispforest.affinity.worldgen.AffinityStructures;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ConfiguredStructureFeatures.class)
public class ConfiguredStructureFeaturesMixin {

    @Inject(method = "registerAll", at = @At("HEAD"))
    private static void injectBikeshed(BiConsumer<ConfiguredStructureFeature<?, ?>, RegistryKey<Biome>> registrar, CallbackInfo ci) {
        registrar.accept(AffinityStructures.CONFIGURED_BIKESHED, AffinityWorldgen.WISP_FOREST_KEY);
    }

}
