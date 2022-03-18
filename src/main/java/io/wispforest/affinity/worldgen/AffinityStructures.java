package io.wispforest.affinity.worldgen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.mixin.access.StructureFeatureAccessor;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;

public class AffinityStructures {

    public static final StructureFeature<?> BIKESHED = new BikeshedFeature();

    public static void register() {
        StructureFeatureAccessor.affinity$register(Affinity.idPlain("bikeshed"), BIKESHED, GenerationStep.Feature.SURFACE_STRUCTURES);
    }

}
