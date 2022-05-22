package io.wispforest.affinity.enchantment.impl;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;

public class AffineEnchantment extends Enchantment {

    public AffineEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.BREAKABLE, EquipmentSlot.values());
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return false;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return false;
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return other != Enchantments.MENDING;
    }
}
