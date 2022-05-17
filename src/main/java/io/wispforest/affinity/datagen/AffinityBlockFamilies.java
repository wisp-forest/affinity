package io.wispforest.affinity.datagen;

import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.data.family.BlockFamily;

public class AffinityBlockFamilies {

    public static final BlockFamily AZALEA = new BlockFamily.Builder(AffinityBlocks.AZALEA_PLANKS)
            .button(AffinityBlocks.AZALEA_BUTTON)
            .fence(AffinityBlocks.AZALEA_FENCE)
            .fenceGate(AffinityBlocks.AZALEA_FENCE_GATE)
            .pressurePlate(AffinityBlocks.AZALEA_PRESSURE_PLATE)
            .sign(AffinityBlocks.AZALEA_SIGN, AffinityBlocks.AZALEA_WALL_SIGN)
            .slab(AffinityBlocks.AZALEA_SLAB)
            .stairs(AffinityBlocks.AZALEA_STAIRS)
            .door(AffinityBlocks.AZALEA_DOOR)
            .trapdoor(AffinityBlocks.AZALEA_TRAPDOOR)
            .group("wooden")
            .unlockCriterionName("has_planks")
            .build();
}
