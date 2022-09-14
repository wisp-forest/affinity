package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class AssemblyAugmentBlockEntity extends BlockEntity {

    public AssemblyAugmentBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASSEMBLY_AUGMENT, pos, state);
    }

}
