package io.wispforest.affinity.statuseffects;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.DamageTypeKey;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.tag.BlockTags;

public class ResonantStatusEffect extends AffinityStatusEffect {

    public static final DamageTypeKey DAMAGE_TYPE = new DamageTypeKey(Affinity.id("resonating"));

    public ResonantStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().getBlockState(entity.getBlockPos().down()).isIn(BlockTags.DAMPENS_VIBRATIONS)) return true;
        entity.damage(DAMAGE_TYPE.source(entity.getWorld()), amplifier + 1);

        return true;
    }
}
