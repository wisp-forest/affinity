package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;

public class CropReaperBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {
    private int roundTime = 0;

    public CropReaperBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.CROP_REAPER, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxExtract(256);
    }

    @Override
    public void tickServer() {
        roundTime++;

        if (roundTime < 20) return;

        roundTime = 0;

        if (this.fluxStorage.flux() >= this.fluxStorage.fluxCapacity()) return;

        int addedAethum = 0;

        for (BlockPos pos : BlockPos.iterate(this.getPos().add(-8, -4, -8), this.pos.add(8, 4, 8))) {
            final var state = world.getBlockState(pos);

            if (!(state.getBlock() instanceof CropBlock)) continue;

            if (state.get(CropBlock.AGE) == 7) {
                world.breakBlock(pos, false);
                world.setBlockState(pos, state.with(CropBlock.AGE, 0));

                addedAethum += 200;
            }
        }

        if (addedAethum > 0) {
            this.updateFlux(Math.min(this.fluxStorage.flux() + addedAethum, this.fluxStorage.fluxCapacity()));
        }
    }
}
