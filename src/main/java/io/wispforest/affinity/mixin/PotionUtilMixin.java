package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(PotionUtil.class)
public class PotionUtilMixin {

    private static ItemStack affinity$itemStack;
    private static StatusEffectInstance affinity$statusEffectInstance;
    private static float affinity$durationMultiplier;

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "buildTooltip", at = @At(value = "INVOKE", target = "net/minecraft/text/Text.translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", ordinal = 1, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void storeStack(ItemStack stack, List<?> list, float durationMultiplier, CallbackInfo ci, List<?> list2, List<?> list3, Iterator<?> var5, StatusEffectInstance statusEffectInstance, MutableText mutableText, StatusEffect statusEffect) {
        affinity$itemStack = stack;
        affinity$statusEffectInstance = statusEffectInstance;
        affinity$durationMultiplier = durationMultiplier;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyArg(method = "buildTooltip", at = @At(value = "INVOKE", target = "net/minecraft/text/Text.translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", ordinal = 1), index = 1)
    private static Object[] addLengthMultiplier(Object[] args) {
        String durationText = (String) args[1];

        if (affinity$itemStack.has(PotionMixture.EXTRA_DATA)) {
            NbtCompound extraData = affinity$itemStack.get(PotionMixture.EXTRA_DATA);

            if (extraData.has(PotionMixture.EXTEND_DURATION_BY)) {
                float extendBy = extraData.get(PotionMixture.EXTEND_DURATION_BY);

                args[1] = durationText + " + " + StringHelper.formatTicks((int) (affinity$statusEffectInstance.getDuration() * affinity$durationMultiplier * (extendBy - 1)));
            }
        }

        affinity$itemStack = null;
        affinity$statusEffectInstance = null;

        return args;
    }

    @Inject(method = "buildTooltip", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void addFuniFlightText(ItemStack stack, List<Text> list, float f, CallbackInfo ci, List<StatusEffectInstance> list2, List<Pair<EntityAttribute, EntityAttributeModifier>> list3) {
        if (list2.stream().noneMatch(statusEffectInstance -> statusEffectInstance.getEffectType() == AffinityStatusEffects.FLIGHT)) return;
        if (list3.isEmpty()) {
            list.add(Text.empty());
            list.add((Text.translatable("potion.whenDrank")).formatted(Formatting.DARK_PURPLE));
        }
        list.add(Text.literal("-9.81 Gravity").formatted(Formatting.BLUE));
    }

}
