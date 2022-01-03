package io.wispforest.affinity.mixin;

import io.wispforest.affinity.worldgen.AffinityWorldgen;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VanillaBiomeParameters.class)
public class VanillaBiomeParametersMixin {

    @Shadow
    @Final
    private RegistryKey<Biome>[][] UNCOMMON_BIOMES;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectForest(CallbackInfo ci) {
        UNCOMMON_BIOMES[1][0] = AffinityWorldgen.WISP_FOREST_KEY;
    }

}
