package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.AffinityDamageEnchantment;
import io.wispforest.affinity.enchantment.template.AffinityEnchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

public class UpdogEnchantment extends AffinityEnchantment implements AffinityDamageEnchantment {
    public UpdogEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, EquipmentSlot.MAINHAND);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean shouldApplyDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage) {
        return true;
    }

    @Override
    public float getExtraDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage) {
        float attackerPercent = attacker.getHealth() / attacker.getMaxHealth();
        float targetPercent = target.getHealth() / target.getMaxHealth();

        return Math.max(0, targetPercent - attackerPercent) * level * incomingDamage;
    }
}
