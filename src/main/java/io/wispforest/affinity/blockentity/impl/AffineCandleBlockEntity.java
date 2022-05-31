package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.ChunkAethumComponent;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

public class AffineCandleBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {
    private int roundTime = 0;

    public AffineCandleBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AFFINE_CANDLE, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxInsert(256);
    }

    @Override
    public void tickServer() {
        boolean isLit = getCachedState().get(Properties.LIT);

        if (!isLit) return;

        long flux = this.fluxStorage.flux();

        if (flux == 0) {
            world.setBlockState(getPos(), getCachedState().with(Properties.LIT, false));
            return;
        }

        updateFlux(flux - 1);

        if (getCachedState().get(CandleBlock.CANDLES) < 3) return;

        roundTime++;

        if (roundTime < 80) return;

        roundTime = 0;


        if (flux <= 50) return;

        ChunkAethumComponent component = AffinityComponents.CHUNK_AETHUM.get(world.getChunk(pos));

        component.setAethum(component.getAethum() + 0.1);

        updateFlux(flux - 50);
    }
}
