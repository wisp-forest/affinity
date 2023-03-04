package io.wispforest.affinity.statuseffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.text.Text;

public class ImpendingDoomStatusEffect extends AffinityStatusEffect {

    public static DamageSource DOOM_DAMAGE = new DoomDamageSource("doom_potion");

    public ImpendingDoomStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        entity.damage(DOOM_DAMAGE, Float.MAX_VALUE);
    }

    public static class DoomDamageSource extends DamageSource {
        public DoomDamageSource(String name) {
            super(name);
            this.setBypassesArmor();
            this.setOutOfWorld();
            this.setUsesMagic();
            this.setUnblockable();
        }

        @Override
        public Text getDeathMessage(LivingEntity entity) {
            return Text.translatable("death.attack." + this.name, entity.getDisplayName());
        }
    }
}
