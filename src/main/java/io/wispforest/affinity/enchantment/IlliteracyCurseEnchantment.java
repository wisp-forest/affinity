package io.wispforest.affinity.enchantment;

import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class IlliteracyCurseEnchantment extends CurseEnchantment {

    public IlliteracyCurseEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR_HEAD, EquipmentSlot.HEAD);
    }
}
