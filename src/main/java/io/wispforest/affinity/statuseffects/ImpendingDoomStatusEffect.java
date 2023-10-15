package io.wispforest.affinity.statuseffects;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.DamageTypeKey;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ImpendingDoomStatusEffect extends AffinityStatusEffect {

    private static final DamageTypeKey DAMAGE_TYPE = new DamageTypeKey(Affinity.id("doom_potion"), DamageTypeKey.Attribution.NEVER_ATTRIBUTE);

    public ImpendingDoomStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public void onRemovedFromEntity(LivingEntity entity) {
        entity.damage(DAMAGE_TYPE.source(entity.getWorld()), Float.MAX_VALUE);
    }
}
