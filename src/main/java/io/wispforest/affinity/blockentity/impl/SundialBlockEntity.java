package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SundialBlockEntity extends AethumNetworkMemberBlockEntity {

    public SundialBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.SUNDIAL, pos, state);
    }

    @Override
    public long flux() {
        return 0;
    }
}
