package io.wispforest.affinity.statuseffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ResonantStatusEffect extends AffinityStatusEffect {

    public static final DamageSource RESONATING = new DamageSource("resonating").setBypassesArmor();

    public ResonantStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        entity.damage(RESONATING, amplifier + 1);
    }
}
