package io.wispforest.affinity.misc;

import net.fabricmc.fabric.api.event.AutoInvokingEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableObject;

public interface BeforeMangroveBasketCaptureCallback {

    @AutoInvokingEvent
    Event<BeforeMangroveBasketCaptureCallback> EVENT = EventFactory.createArrayBacked(BeforeMangroveBasketCaptureCallback.class, callbacks -> (world, pos, state, blockEntity) -> {
        for (var callback : callbacks) {
            if (!callback.beforeMangroveBasketCapture(world, pos, state, blockEntity)) {
                return false;
            }
        }

        if (blockEntity instanceof BeforeMangroveBasketCaptureCallback callback) {
            return callback.beforeMangroveBasketCapture(world, pos, state, blockEntity);
        }

        return true;
    });

    boolean beforeMangroveBasketCapture(World world, BlockPos pos, MutableObject<BlockState> state, BlockEntity blockEntity);
}
