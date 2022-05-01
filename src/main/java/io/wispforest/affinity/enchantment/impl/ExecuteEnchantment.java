package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.AffinityEnchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class ExecuteEnchantment extends AffinityEnchantment {

    public ExecuteEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, EquipmentSlot.MAINHAND);
    }

}
