package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

public class WorldPinBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {
    public WorldPinBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.WORLD_PIN, pos, state);

        this.fluxStorage.setFluxCapacity(80000);
        this.fluxStorage.setMaxInsert(64);
    }

    @Override
    public void tickServer() {
        long flux = flux();
        boolean shouldBeEnabled = flux >= 4;

        if (shouldBeEnabled != getCachedState().get(Properties.ENABLED)) {
            world.setBlockState(pos, getCachedState().with(Properties.ENABLED, shouldBeEnabled));

            if (shouldBeEnabled) {
                AffinityComponents.WORLD_PINS.get(world).addPin(pos, 4);
            } else {
                AffinityComponents.WORLD_PINS.get(world).removePin(pos, 4);
            }
        }

        if (shouldBeEnabled) {
            updateFlux(flux - 4);
        }
    }


    @Override
    public void onBroken() {
        super.onBroken();

        AffinityComponents.WORLD_PINS.get(world).removePin(pos, 4);
    }
}
