package io.wispforest.affinity.worldgen;

import io.wispforest.affinity.Affinity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.structure.StructureType;

public class AffinityStructures {

    public static StructureType<BikeshedStructure> BIKESHED;

    public static void register() {
        BIKESHED = Registry.register(Registry.STRUCTURE_TYPE, Affinity.id("bikeshed"), () -> BikeshedStructure.CODEC);
    }

}
