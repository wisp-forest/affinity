package com.glisco.nidween.mixin;

import com.glisco.nidween.registries.NidweenStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "applyDamage", at = @At("TAIL"))
    public void applyLifeLeech(DamageSource source, float amount, CallbackInfo ci) {
        if (!(source.getAttacker() instanceof PlayerEntity player)) return;
        if (!player.hasStatusEffect(NidweenStatusEffects.LIFE_LEECH)) return;

        player.heal(amount * 0.1f * (player.getStatusEffect(NidweenStatusEffects.LIFE_LEECH).getAmplifier() + 1));
    }

}
