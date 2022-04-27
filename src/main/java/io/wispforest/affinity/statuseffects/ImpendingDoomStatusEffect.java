package io.wispforest.affinity.statuseffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ImpendingDoomStatusEffect extends AffinityStatusEffect {

    public static DamageSource DOOM_DAMAGE = new DoomDamageSource();

    public ImpendingDoomStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        entity.damage(DOOM_DAMAGE, Float.MAX_VALUE);
    }

    private static class DoomDamageSource extends DamageSource {
        protected DoomDamageSource() {
            super("doom_potion");
            this.setBypassesArmor();
            this.setOutOfWorld();
            this.setUsesMagic();
            this.setUnblockable();
        }
    }
}
