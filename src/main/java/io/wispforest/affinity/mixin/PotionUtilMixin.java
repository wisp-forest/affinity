package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PotionUtil.class)
public class PotionUtilMixin {
    @Inject(method = "buildTooltip(Lnet/minecraft/item/ItemStack;Ljava/util/List;FF)V", at = @At("HEAD"))
    private static void storeStack(ItemStack stack, List<Text> list, float durationMultiplier, float tickRate, CallbackInfo ci) {
        MixinHooks.POTION_UTIL_STACK.set(stack);
    }

    @Inject(method = "buildTooltip(Lnet/minecraft/item/ItemStack;Ljava/util/List;FF)V", at = @At("TAIL"))
    private static void releaseStack(ItemStack stack, List<Text> list, float durationMultiplier, float tickRate, CallbackInfo ci) {
        MixinHooks.POTION_UTIL_STACK.remove();
    }

    @Inject(method = "buildTooltip(Ljava/util/List;Ljava/util/List;FF)V", at = @At(value = "INVOKE", target = "net/minecraft/text/Text.translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", ordinal = 1, shift = At.Shift.BEFORE))
    private static void storeData(List<StatusEffectInstance> statusEffects, List<Text> list, float durationMultiplier, float tickRate, CallbackInfo ci, @Local StatusEffectInstance statusEffectInstance) {
        MixinHooks.POTION_UTIL_DATA.set(new MixinHooks.PotionUtilData(statusEffectInstance, durationMultiplier));
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyArg(method = "buildTooltip(Ljava/util/List;Ljava/util/List;FF)V", at = @At(value = "INVOKE", target = "net/minecraft/text/Text.translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", ordinal = 1), index = 1)
    private static Object[] addLengthMultiplier(Object[] args, @Local(argsOnly = true, ordinal = 1) float tickRate) {
        if (!(args[1] instanceof MutableText text && text.getContent() instanceof PlainTextContent.Literal literal)) {
            return args;
        }
        String durationText = literal.string();

        var data = MixinHooks.POTION_UTIL_DATA.get();
        var stack = MixinHooks.POTION_UTIL_STACK.get();

        if (stack == null) {
            MixinHooks.POTION_UTIL_DATA.remove();
            return args;
        }

        if (stack.has(PotionMixture.EXTRA_DATA)) {
            NbtCompound extraData = stack.get(PotionMixture.EXTRA_DATA);

            if (extraData.has(PotionMixture.EXTEND_DURATION_BY)) {
                float extendBy = extraData.get(PotionMixture.EXTEND_DURATION_BY);

                args[1] = durationText + " + " + StringHelper.formatTicks((int) (data.effectInst().getDuration() * data.durationMultiplier() * (extendBy - 1)), tickRate);
            }
        }

        MixinHooks.POTION_UTIL_DATA.remove();

        return args;
    }

    @Inject(method = "buildTooltip(Ljava/util/List;Ljava/util/List;FF)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void addFuniFlightText(List<StatusEffectInstance> effects, List<Text> tooltip, float durationMultiplier, float tickRate, CallbackInfo ci, List<Pair<EntityAttribute, EntityAttributeModifier>> attributeModifiers) {
        if (effects.stream().noneMatch(statusEffectInstance -> statusEffectInstance.getEffectType() == AffinityStatusEffects.FLIGHT)) {
            return;
        }

        if (attributeModifiers.isEmpty()) {
            tooltip.add(Text.empty());
            tooltip.add((Text.translatable("potion.whenDrank")).formatted(Formatting.DARK_PURPLE));
        }
        tooltip.add(Text.translatable("effect.affinity.gravity_modifier").formatted(Formatting.BLUE));
    }

}
