package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionUtil;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.mixin.access.StatusEffectInstanceAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void doPotionApplication(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        PotionUtil.getPotionEffects(stack).forEach(effect -> MixinHooks.potionApplied(effect, user, stack.getComponents()));
    }

    @Inject(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/PotionContentsComponent;forEachEffect(Ljava/util/function/Consumer;)V"))
    private void captureStack(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        MixinHooks.POTION_ITEM_STACK.set(stack);
    }

    @ModifyArg(method = "method_57389", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"))
    private static StatusEffectInstance extendPotionEffect(StatusEffectInstance effect) {
        var stack = MixinHooks.POTION_ITEM_STACK.get();

        if (stack == null) return effect;

        ((StatusEffectInstanceAccessor) effect).setDuration((int) (effect.getDuration() * stack.getOrDefault(PotionMixture.EXTEND_DURATION_BY, 1f)));

        return effect;
    }

    @Inject(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/PotionContentsComponent;forEachEffect(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void releaseStack(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        MixinHooks.POTION_ITEM_STACK.remove();
    }
}
