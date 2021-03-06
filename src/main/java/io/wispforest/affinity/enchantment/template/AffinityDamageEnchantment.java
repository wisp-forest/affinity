package io.wispforest.affinity.enchantment.template;

import net.minecraft.entity.LivingEntity;

public interface AffinityDamageEnchantment {

    boolean shouldApplyDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage);

    float getExtraDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage);

}
