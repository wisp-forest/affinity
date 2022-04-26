package io.wispforest.affinity.misc;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

public interface LivingEntityTickEvent {

    Event<LivingEntityTickEvent> EVENT = EventFactory.createArrayBacked(LivingEntityTickEvent.class, callbacks -> entity -> {
        for (var callback : callbacks) {
            callback.onTick(entity);
        }
    });

    void onTick(LivingEntity entity);

}
