package io.wispforest.affinity.mixin;

import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TreeConfiguredFeatures.class)
public interface TreeConfigureFeaturesAccessor {

    @Mutable
    @Accessor("AZALEA_TREE")
    static void setAzaleaTree(ConfiguredFeature<?, ?> feature) {
        throw new AssertionError("Illegal call to accessor body");
    }

}
