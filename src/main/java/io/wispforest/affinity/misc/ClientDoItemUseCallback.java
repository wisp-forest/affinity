package io.wispforest.affinity.misc;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public interface ClientDoItemUseCallback {

    Event<ClientDoItemUseCallback> EVENT = EventFactory.createArrayBacked(ClientDoItemUseCallback.class, listeners -> (player, hand) -> {
        for (var listener : listeners) {
            var result = listener.doItemUse(player, hand);
            if (result.isAccepted()) return result;
        }

        return ActionResult.PASS;
    });

    ActionResult doItemUse(PlayerEntity player, Hand hand);

}
