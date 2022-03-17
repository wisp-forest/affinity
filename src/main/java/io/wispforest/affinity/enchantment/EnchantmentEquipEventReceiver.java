package io.wispforest.affinity.enchantment;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * An interface to be implemented by enchantments that wish
 * to be notified when the item they are on is equipped/unequipped
 */
public interface EnchantmentEquipEventReceiver {

    void onEquip(LivingEntity entity, EquipmentSlot slot, ItemStack stack);

    void onUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack);
}
