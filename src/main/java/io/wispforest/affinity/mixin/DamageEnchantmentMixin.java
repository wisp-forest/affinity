package io.wispforest.affinity.mixin;

import io.wispforest.affinity.enchantment.template.AffinityDamageEnchantment;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DamageEnchantment.class)
public class DamageEnchantmentMixin implements AffinityDamageEnchantment {

    @Shadow @Final public int typeIndex;

    @Override
    public boolean shouldApplyDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage) {
        if (typeIndex != 1) return false;

        return target.hasStatusEffect(AffinityStatusEffects.UNHOLY);
    }

    @Override
    public float getExtraDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage) {
        return 2.5f * level * (1 + target.getStatusEffect(AffinityStatusEffects.UNHOLY).getAmplifier());
    }
}
