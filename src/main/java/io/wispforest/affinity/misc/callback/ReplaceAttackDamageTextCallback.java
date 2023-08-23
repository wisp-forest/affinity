package io.wispforest.affinity.misc.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.Nullable;

public interface ReplaceAttackDamageTextCallback {

    Event<ReplaceAttackDamageTextCallback> EVENT = EventFactory.createArrayBacked(ReplaceAttackDamageTextCallback.class, callbacks -> stack -> {
        for (var callback : callbacks) {
            var replacement = callback.replaceDamageText(stack);
            if (replacement == null) continue;

            return replacement;
        }

        return null;
    });

    @Nullable MutableText replaceDamageText(ItemStack stack);
}
