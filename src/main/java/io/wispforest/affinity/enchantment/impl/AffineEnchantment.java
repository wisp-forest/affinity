// TODO: port this.

//package io.wispforest.affinity.enchantment.impl;
//
//import io.wispforest.affinity.enchantment.template.AffinityEnchantment;
//import net.minecraft.enchantment.Enchantment;
//import net.minecraft.enchantment.EnchantmentTarget;
//import net.minecraft.enchantment.Enchantments;
//import net.minecraft.entity.EquipmentSlot;
//
//public class AffineEnchantment extends AffinityEnchantment {
//
//    public AffineEnchantment() {
//        super(Rarity.RARE, EnchantmentTarget.BREAKABLE, EquipmentSlot.values());
//    }
//
//    @Override
//    public boolean isTreasure() {
//        return true;
//    }
//
//    @Override
//    protected boolean canAccept(Enchantment other) {
//        return other != Enchantments.MENDING && super.canAccept(other);
//    }
//}
