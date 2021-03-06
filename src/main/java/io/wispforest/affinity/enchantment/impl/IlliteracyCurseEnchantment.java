package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.EnchantmentEquipEventReceiver;
import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class IlliteracyCurseEnchantment extends CurseEnchantment implements EnchantmentEquipEventReceiver {

    public IlliteracyCurseEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR_HEAD, EquipmentSlot.HEAD);
    }

    @Override
    public void onEquip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.HEAD) return;
        MixinHooks.TEXT_OBFUSCATION = true;
    }

    @Override
    public void onUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.HEAD) return;
        MixinHooks.TEXT_OBFUSCATION = false;
    }
}
