package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

public class WorldPinBlockEntity extends BlockEntity implements TickedBlockEntity {
    public WorldPinBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.WORLD_PIN, pos, state);
    }

    @Override
    public void tickServer() {
        var shouldBeEnabled = !this.world.isReceivingRedstonePower(this.pos);

        if (shouldBeEnabled != getCachedState().get(Properties.ENABLED)) {
            this.world.setBlockState(pos, getCachedState().with(Properties.ENABLED, shouldBeEnabled));

            if (shouldBeEnabled) {
                this.world.getComponent(AffinityComponents.WORLD_PINS).addPin(pos, 4);
            } else {
                this.world.getComponent(AffinityComponents.WORLD_PINS).removePin(pos, 4);
            }
        }
    }

    public void onBroken() {
        this.world.getComponent(AffinityComponents.WORLD_PINS).removePin(pos, 4);
    }
}
