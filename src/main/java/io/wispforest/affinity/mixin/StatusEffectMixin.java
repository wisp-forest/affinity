package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.components.AffinityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffect.class)
public class StatusEffectMixin {

    @Inject(method = "onRemoved", at = @At("HEAD"))
    private void removeGlowingColor(LivingEntity entity, AttributeContainer attributes, int amplifier, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity player)) return;
        AffinityComponents.GLOWING_COLOR.get(player).reset();
    }

}
