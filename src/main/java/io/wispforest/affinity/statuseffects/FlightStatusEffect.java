package io.wispforest.affinity.statuseffects;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

public class FlightStatusEffect extends AffinityStatusEffect {

    public static final AbilitySource FLIGHT_POTION = Pal.getAbilitySource(Affinity.id("flight_potion"));

    public FlightStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            Pal.grantAbility(player, VanillaAbilities.ALLOW_FLYING, FLIGHT_POTION);
        } else {
            entity.setNoGravity(true);
        }
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient()) return;
        entity.playSound(SoundEvents.UI_TOAST_IN, 1, entity.getStatusEffect(AffinityStatusEffects.FLIGHT).getDuration() == 200 ? .75f : .25f);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration == 200 || duration == 100;
    }

    @Override
    public void onRemovedFromEntity(LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity player) {
            Pal.revokeAbility(player, VanillaAbilities.ALLOW_FLYING, FLIGHT_POTION);
        } else {
            entity.setNoGravity(false);
        }
    }
}
