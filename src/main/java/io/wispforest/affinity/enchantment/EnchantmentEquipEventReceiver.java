package io.wispforest.affinity.enchantment;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface EnchantmentEquipEventReceiver {

    void onEquip(LivingEntity entity, EquipmentSlot slot, ItemStack stack);

    void onUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack);

}
