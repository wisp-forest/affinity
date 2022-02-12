package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class RitualCoreBlockEntity extends AethumNetworkMemberBlockEntity {

    public RitualCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.RITUAL_CORE, pos, state);
    }
}
