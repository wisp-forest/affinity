package io.wispforest.affinity.statuseffects;

import io.wispforest.affinity.object.AffinityParticleSystems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;

public class DrippingStatusEffect extends AffinityStatusEffect {

    public DrippingStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.world.random.nextDouble() > .05) return;
        AffinityParticleSystems.DRIPPING_AZALEA.spawn(entity.getWorld(), entity.getPos().add(0, 1, 0));
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
