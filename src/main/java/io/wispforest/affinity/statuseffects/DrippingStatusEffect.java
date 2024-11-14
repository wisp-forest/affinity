package io.wispforest.affinity.statuseffects;

import io.wispforest.affinity.object.AffinityParticleSystems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;

public class DrippingStatusEffect extends AffinityStatusEffect {

    public DrippingStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().random.nextDouble() > .05) return true;
        AffinityParticleSystems.DRIPPING_AZALEA.spawn(entity.getWorld(), entity.getPos().add(0, 1, 0));

        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
