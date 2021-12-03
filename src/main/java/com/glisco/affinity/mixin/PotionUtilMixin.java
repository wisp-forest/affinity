package com.glisco.affinity.mixin;

import com.glisco.affinity.registries.AffinityStatusEffects;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PotionUtil.class)
public class PotionUtilMixin {

    @Inject(method = "buildTooltip", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void addFuniFlightText(ItemStack stack, List<Text> list, float f, CallbackInfo ci, List<StatusEffectInstance> list2, List<Pair<EntityAttribute, EntityAttributeModifier>> list3) {
        if (list2.stream().noneMatch(statusEffectInstance -> statusEffectInstance.getEffectType() == AffinityStatusEffects.FLIGHT)) return;
        if (list3.isEmpty()) {
            list.add(LiteralText.EMPTY);
            list.add((new TranslatableText("potion.whenDrank")).formatted(Formatting.DARK_PURPLE));
        }
        list.add(new LiteralText("-9.81 Gravity").formatted(Formatting.BLUE));
    }

}
