package io.wispforest.affinity.mixin;

import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectUtil.class)
public class StatusEffectUtilMixin {

    @Inject(method = "getDurationText", at = @At("HEAD"), cancellable = true)
    private static void injectBastionRegenerationName(StatusEffectInstance effect, float multiplier, float tickRate, CallbackInfoReturnable<Text> cir) {
        if (effect.getEffectType() != AffinityStatusEffects.BASTION_REGENERATION) return;
        cir.setReturnValue(StatusEffects.REGENERATION.value().getName().copy().formatted(Formatting.WHITE));
    }

}
