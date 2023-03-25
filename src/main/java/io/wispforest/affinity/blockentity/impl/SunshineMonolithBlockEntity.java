package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.SunshineMonolithBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

public class SunshineMonolithBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {
    public SunshineMonolithBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.SUNSHINE_MONOLITH, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxInsert(64);
    }

    @Override
    public void tickServer() {
        long flux = flux();
        boolean shouldBeEnabled = flux >= 1;

        if (shouldBeEnabled != this.getCachedState().get(SunshineMonolithBlock.ENABLED)) {
            this.world.setBlockState(this.pos, this.getCachedState().with(SunshineMonolithBlock.ENABLED, shouldBeEnabled));

            if (shouldBeEnabled) {
                this.addMonolithToChunks();
            } else {
                this.removeMonolithFromChunks();
            }
        }

        if (shouldBeEnabled && this.world.isRaining()) {
            this.updateFlux(flux - 1);
        }
    }


    @Override
    public void onBroken() {
        super.onBroken();
        this.removeMonolithFromChunks();
    }

    private void addMonolithToChunks() {
        int blockChunkX = this.pos.getX() >> 4;
        int blockChunkZ = this.pos.getZ() >> 4;
        int radius = 3;

        for (int x = blockChunkX - radius; x <= blockChunkX + radius; x++) {
            for (int z = blockChunkZ - radius; z <= blockChunkZ + radius; z++) {
                AffinityComponents.LOCAL_WEATHER.get(world.getChunk(x, z)).addMonolith(this.pos);
            }
        }
    }

    private void removeMonolithFromChunks() {
        int blockChunkX = this.pos.getX() >> 4;
        int blockChunkZ = this.pos.getZ() >> 4;
        int radius = 3;

        for (int x = blockChunkX - radius; x <= blockChunkX + radius; x++) {
            for (int z = blockChunkZ - radius; z <= blockChunkZ + radius; z++) {
                AffinityComponents.LOCAL_WEATHER.get(world.getChunk(x, z)).removeMonolith(this.pos);
            }
        }
    }
}
