package io.wispforest.affinity.blockentity;

import io.wispforest.affinity.registries.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
