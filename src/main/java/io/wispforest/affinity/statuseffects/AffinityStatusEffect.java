package io.wispforest.affinity.statuseffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class AffinityStatusEffect extends StatusEffect {

    public AffinityStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    public void onPotionApplied(LivingEntity target, @Nullable NbtCompound extraData) {}
    public void onRemovedFromEntity(LivingEntity entity) {}
}
