package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class AethumFluxCacheBlockEntity extends AethumNetworkMemberBlockEntity {

    public AethumFluxCacheBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, pos, state);

        this.fluxStorage.setFluxCapacity(128000);
        this.fluxStorage.setMaxExtract(512);
    }

}
