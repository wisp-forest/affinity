package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SundialBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    public SundialBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.SUNDIAL, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxExtract(256);
    }

    @Override
    public void tickServer() {
        var flux = this.fluxStorage.flux();
        if (flux >= this.fluxStorage.fluxCapacity()) return;

        this.updateFlux(Math.min(flux + 20, this.fluxStorage.fluxCapacity()));
    }
}
