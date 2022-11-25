package io.wispforest.affinity.misc;

import net.fabricmc.fabric.api.event.AutoInvokingEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PreMangroveBasketCallback {
    @AutoInvokingEvent
    Event<PreMangroveBasketCallback> EVENT = EventFactory.createArrayBacked(PreMangroveBasketCallback.class, callbacks -> (world, pos, state, blockEntity) -> {
        for (var callback : callbacks) {
            if (!callback.preMangroveBasket(world, pos, state, blockEntity)) {
                return false;
            }
        }

        if (blockEntity instanceof PreMangroveBasketCallback callback) {
            return callback.preMangroveBasket(world, pos, state, blockEntity);
        }

        return true;
    });

    boolean preMangroveBasket(World world, BlockPos pos, BlockState state, BlockEntity blockEntity);
}
