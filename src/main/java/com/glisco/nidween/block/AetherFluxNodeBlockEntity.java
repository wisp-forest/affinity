package com.glisco.nidween.block;

import com.glisco.nidween.registries.NidweenBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class AetherFluxNodeBlockEntity extends BlockEntity {

    public AetherFluxNodeBlockEntity(BlockPos pos, BlockState state) {
        super(NidweenBlocks.Entities.AETHER_FLUX_NODE, pos, state);
    }
}
