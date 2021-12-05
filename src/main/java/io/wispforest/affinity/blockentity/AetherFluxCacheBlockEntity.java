package io.wispforest.affinity.blockentity;

import io.wispforest.affinity.registries.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class AetherFluxCacheBlockEntity extends AetherNetworkMemberBlockEntity {

    public AetherFluxCacheBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHER_FLUX_CACHE, pos, state);
    }

    @Override
    public long flux() {
        return 0;
    }
}
