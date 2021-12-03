package io.wispforest.affinity.statuseffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;

public class FlightStatusEffect extends AffinityStatusEffect {

    public FlightStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            player.getAbilities().allowFlying = true;
            player.sendAbilitiesUpdate();
        } else {
            entity.setNoGravity(true);
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            player.interactionManager.getGameMode().setAbilities(player.getAbilities());
            player.sendAbilitiesUpdate();
        } else {
            entity.setNoGravity(false);
        }

    }
}
