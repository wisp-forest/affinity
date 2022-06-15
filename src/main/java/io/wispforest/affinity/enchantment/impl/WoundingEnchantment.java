package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.AffinityEnchantment;
import io.wispforest.affinity.object.AffinityEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class WoundingEnchantment extends AffinityEnchantment {

    public WoundingEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, EquipmentSlot.MAINHAND);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return other != AffinityEnchantments.CRITICAL_GAMBLE && super.canAccept(other);
    }
}
