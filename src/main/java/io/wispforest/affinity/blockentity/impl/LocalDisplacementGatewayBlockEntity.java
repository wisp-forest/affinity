package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class LocalDisplacementGatewayBlockEntity extends BlockEntity {
    public LocalDisplacementGatewayBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.LOCAL_DISPLACEMENT_GATEWAY, pos, state);
    }
}
