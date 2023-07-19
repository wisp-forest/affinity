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
        long flux = this.flux();
        boolean shouldBeEnabled = flux >= 4;

        if (shouldBeEnabled != getCachedState().get(Properties.ENABLED)) {
            this.world.setBlockState(pos, getCachedState().with(Properties.ENABLED, shouldBeEnabled));

            if (shouldBeEnabled) {
                this.world.getComponent(AffinityComponents.WORLD_PINS).addPin(pos, 4);
            } else {
                this.world.getComponent(AffinityComponents.WORLD_PINS).removePin(pos, 4);
            }
        }

        if (shouldBeEnabled) {
            this.updateFlux(flux - 4);
        }
    }


    @Override
    public void onBroken() {
        super.onBroken();

        this.world.getComponent(AffinityComponents.WORLD_PINS).removePin(pos, 4);
    }
}
