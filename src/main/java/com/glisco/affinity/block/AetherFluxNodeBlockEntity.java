package com.glisco.affinity.block;

import com.glisco.affinity.registries.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class AetherFluxNodeBlockEntity extends BlockEntity {

    public AetherFluxNodeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHER_FLUX_NODE, pos, state);
    }
}
