package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.ChunkAethumComponent;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
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
        boolean shouldBeLit = this.fluxStorage.flux() > 0;
        if (getCachedState().get(Properties.LIT) != shouldBeLit) {
            world.setBlockState(getPos(), getCachedState().with(Properties.LIT, shouldBeLit));
        }

        roundTime++;

        if (roundTime < 80) return;

        roundTime = 0;

        long flux = this.fluxStorage.flux();

        if (flux <= 50) return;

        ChunkAethumComponent component = AffinityComponents.CHUNK_AETHUM.get(world.getChunk(pos));

        component.setAethum(component.getAethum() + 0.1);

        updateFlux(flux - 50);
    }
}
