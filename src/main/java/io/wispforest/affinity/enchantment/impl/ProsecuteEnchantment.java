package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.AffinityDamageEnchantment;
import io.wispforest.affinity.enchantment.template.AffinityEnchantment;
import io.wispforest.affinity.object.AffinityEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

public class ProsecuteEnchantment extends AffinityEnchantment implements AffinityDamageEnchantment {

    public ProsecuteEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, EquipmentSlot.MAINHAND);
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return other != AffinityEnchantments.EXECUTE;
    }

    @Override
    public boolean shouldApplyDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage) {
        return target.getHealth() == target.getMaxHealth();
    }

    @Override
    public float getExtraDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage) {
        return incomingDamage * 0.2f;
    }
}
