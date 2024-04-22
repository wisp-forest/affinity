package io.wispforest.affinity.blockentity.template;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;

public interface TickedBlockEntity {

    default void tickClient() {}

    default void tickServer() {}

    static <T extends BlockEntity & TickedBlockEntity> BlockEntityTicker<T> ticker() {
        return (world, pos, state, blockEntity) -> {
            if (world.isClient) {blockEntity.tickClient();} else blockEntity.tickServer();
        };
    }

}
