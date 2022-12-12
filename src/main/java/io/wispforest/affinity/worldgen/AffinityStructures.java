package io.wispforest.affinity.worldgen;

import io.wispforest.affinity.Affinity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.structure.StructureType;

public class AffinityStructures {

    public static StructureType<BikeshedStructure> BIKESHED;

    public static void register() {
        BIKESHED = Registry.register(Registries.STRUCTURE_TYPE, Affinity.id("bikeshed"), () -> BikeshedStructure.CODEC);
    }

}
