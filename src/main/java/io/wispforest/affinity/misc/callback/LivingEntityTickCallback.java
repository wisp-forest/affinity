package io.wispforest.affinity.misc.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

public interface LivingEntityTickCallback {

    Event<LivingEntityTickCallback> EVENT = EventFactory.createArrayBacked(LivingEntityTickCallback.class, callbacks -> entity -> {
        for (var callback : callbacks) {
            callback.onTick(entity);
        }
    });

    void onTick(LivingEntity entity);

}
