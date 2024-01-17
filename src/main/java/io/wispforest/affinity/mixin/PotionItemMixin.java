package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.mixin.access.StatusEffectInstanceAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void doPotionApplication(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        final var extraData = stack.get(PotionMixture.EXTRA_DATA);
        PotionUtil.getPotionEffects(stack).forEach(effect -> MixinHooks.potionApplied(effect, user, extraData));
    }

    @ModifyArg(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"))
    private StatusEffectInstance extendPotionEffect(StatusEffectInstance effect, @Local(argsOnly = true) ItemStack stack) {
        if (!stack.has(PotionMixture.EXTRA_DATA)) return effect;

        final var extraData = stack.get(PotionMixture.EXTRA_DATA);
        if (extraData.has(PotionMixture.EXTEND_DURATION_BY)) {
            ((StatusEffectInstanceAccessor) effect).setDuration((int) (effect.getDuration() * extraData.get(PotionMixture.EXTEND_DURATION_BY)));
        }

        return effect;
    }
}
