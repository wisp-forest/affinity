package io.wispforest.affinity.misc;

import io.wispforest.affinity.enchantment.AffinityDamageEnchantment;
import io.wispforest.affinity.enchantment.BerserkerEnchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

public class MixinHooks {

    public static boolean TEXT_OBFUSCATION = false;

    public static float getExtraAttackDamage(LivingEntity attacker, Entity entity, float baseAmount) {
        if (!(entity instanceof LivingEntity target)) return baseAmount;

        float extraDamage = 0;

        final var enchantments = EnchantmentHelper.get(attacker.getMainHandStack());
        for (var enchantment : enchantments.keySet()) {
            if (!(enchantment instanceof AffinityDamageEnchantment damageEnchantment)) continue;

            final int level = enchantments.get(enchantment);
            if (!damageEnchantment.shouldApplyDamage(level, attacker, target, baseAmount)) continue;

            extraDamage += damageEnchantment.getExtraDamage(level, attacker, target, baseAmount);
        }

        if (AffinityEntityAddon.getData(attacker, BerserkerEnchantment.BERSERK_KEY)) {
            extraDamage += (1 - (attacker.getHealth() / attacker.getMaxHealth())) * (1 - (attacker.getHealth() / attacker.getMaxHealth())) * 15;
            attacker.sendSystemMessage(Text.of("Extra damage: " + extraDamage), null);
        }

        return baseAmount + extraDamage;
    }

}
