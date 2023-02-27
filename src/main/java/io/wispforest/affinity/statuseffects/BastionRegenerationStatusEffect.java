package io.wispforest.affinity.statuseffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;

public class BastionRegenerationStatusEffect extends AffinityStatusEffect {

    public BastionRegenerationStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        entity.heal((1 - entity.getHealth() / entity.getMaxHealth()) * 2);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % (10 - amplifier) == 0;
    }
}
