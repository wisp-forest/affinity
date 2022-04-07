package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class AberrantCallingCoreBlockEntity extends RitualCoreBlockEntity {

    public AberrantCallingCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ABERRANT_CALLING_CORE, pos, state);
    }

    @Override
    protected boolean onRitualStart(RitualSetup setup) {
        return false;
    }

    @Override
    protected void doRitualTick() {

    }

    @Override
    protected boolean onRitualCompleted() {
        return false;
    }

    @Override
    protected boolean onRitualInterrupted() {
        return false;
    }
}
