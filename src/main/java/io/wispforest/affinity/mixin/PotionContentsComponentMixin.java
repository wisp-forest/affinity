package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.quack.ExtendedPotionContentsComponent;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

@Mixin(PotionContentsComponent.class)
public class PotionContentsComponentMixin implements ExtendedPotionContentsComponent {
    @Unique private WeakReference<ItemStack> affinity$stackRef;

    @Override
    public void affinity$attackStack(ItemStack stack) {
        affinity$stackRef = new WeakReference<>(stack);
    }

    @Inject(method = "buildTooltip(Ljava/util/function/Consumer;FF)V", at = @At("HEAD"))
    private void storeStack(Consumer<Text> textConsumer, float durationMultiplier, float tickRate, CallbackInfo ci) {
        ItemStack stack = affinity$stackRef == null ? null : affinity$stackRef.get();

        if (stack != null) MixinHooks.POTION_CONTENTS_COMPONENT_STACK.set(stack);
    }

    @Inject(method = "buildTooltip(Ljava/util/function/Consumer;FF)V", at = @At("TAIL"))
    private void releaseStack(Consumer<Text> textConsumer, float durationMultiplier, float tickRate, CallbackInfo ci) {
        MixinHooks.POTION_CONTENTS_COMPONENT_STACK.remove();
    }

    @ModifyArg(method = "buildTooltip(Ljava/lang/Iterable;Ljava/util/function/Consumer;FF)V", at = @At(value = "INVOKE", target = "net/minecraft/text/Text.translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", ordinal = 1), index = 1)
    private static Object[] addLengthMultiplier(Object[] args, @Local(argsOnly = true, ordinal = 0) float durationMultiplier, @Local(argsOnly = true, ordinal = 1) float tickRate, @Local StatusEffectInstance effectInst) {
        if (!(args[1] instanceof MutableText text && text.getContent() instanceof PlainTextContent.Literal literal)) {
            return args;
        }
        String durationText = literal.string();

        var stack = MixinHooks.POTION_CONTENTS_COMPONENT_STACK.get();

        if (stack == null) {
            return args;
        }

        if (stack.contains(PotionMixture.EXTEND_DURATION_BY)) {
            float extendBy = stack.get(PotionMixture.EXTEND_DURATION_BY);

            args[1] = durationText + " + " + StringHelper.formatTicks((int) (effectInst.getDuration() * durationMultiplier * (extendBy - 1)), tickRate);
        }

        return args;
    }

    @Inject(method = "buildTooltip(Ljava/lang/Iterable;Ljava/util/function/Consumer;FF)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void addFuniFlightText(Iterable<StatusEffectInstance> effects, Consumer<Text> tooltip, float durationMultiplier, float tickRate, CallbackInfo ci, List<Pair<EntityAttribute, EntityAttributeModifier>> attributeModifiers) {
        if (StreamSupport.stream(effects.spliterator(), false).noneMatch(statusEffectInstance -> statusEffectInstance.getEffectType().value() == AffinityStatusEffects.FLIGHT)) {
            return;
        }

        if (attributeModifiers.isEmpty()) {
            tooltip.accept(Text.empty());
            tooltip.accept((Text.translatable("potion.whenDrank")).formatted(Formatting.DARK_PURPLE));
        }
        tooltip.accept(Text.translatable("effect.affinity.gravity_modifier").formatted(Formatting.BLUE));
    }
}
