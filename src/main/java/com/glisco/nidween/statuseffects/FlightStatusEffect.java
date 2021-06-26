package com.glisco.nidween.statuseffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.server.network.ServerPlayerEntity;

public class FlightStatusEffect extends NidweenStatusEffect {

    public FlightStatusEffect(StatusEffectType type, int color) {
        super(type, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (!(entity instanceof ServerPlayerEntity player)) return;
        player.getAbilities().allowFlying = true;
        player.sendAbilitiesUpdate();
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (!(entity instanceof ServerPlayerEntity player)) return;

        player.interactionManager.getGameMode().setAbilities(player.getAbilities());
        player.sendAbilitiesUpdate();
    }
}
