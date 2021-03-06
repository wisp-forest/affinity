package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

public class AffineCandleBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    public AffineCandleBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AFFINE_CANDLE, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxInsert(256);
    }

    @Override
    public void tickServer() {
        if (!getCachedState().get(Properties.LIT)) return;

        long flux = this.fluxStorage.flux();
        if (flux > 0) updateFlux(flux - 1);

        if (getCachedState().get(CandleBlock.CANDLES) < 3) return;
        if (this.world.getTime() % 80 != 0) return;
        if (flux <= 50) return;

        var component = AffinityComponents.CHUNK_AETHUM.get(world.getChunk(pos));
        component.setAethum(component.getAethum() + 0.1);

        updateFlux(flux - 50);
    }
}
