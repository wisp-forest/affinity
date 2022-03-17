package io.wispforest.affinity.enchantment;

import io.wispforest.affinity.misc.MixinStates;
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
        MixinStates.TEXT_OBFUSCATION = true;
    }

    @Override
    public void onUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        MixinStates.TEXT_OBFUSCATION = false;
    }
}
