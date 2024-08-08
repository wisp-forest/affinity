package io.wispforest.affinity.misc.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public final class ItemEquipEvents {
    public static final Event<Equip> EQUIP = EventFactory.createArrayBacked(Equip.class, handlers -> (entity, slot, stack) -> {
        for (var handler : handlers) {
            handler.onItemEquip(entity, slot, stack);
        }
    });

    public static final Event<Unequip> UNEQUIP = EventFactory.createArrayBacked(Unequip.class, handlers -> (entity, slot, stack) -> {
        for (var handler : handlers) {
            handler.onItemUnequip(entity, slot, stack);
        }
    });

    private ItemEquipEvents() { }

    public interface Equip {
        void onItemEquip(LivingEntity entity, EquipmentSlot slot, ItemStack stack);
    }

    public interface Unequip {
        void onItemUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack);
    }
}
