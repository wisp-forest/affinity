package io.wispforest.affinity.misc;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.enchantment.impl.BerserkerEnchantment;
import io.wispforest.affinity.enchantment.template.AffinityDamageEnchantment;
import io.wispforest.affinity.item.WispMistItem;
import io.wispforest.affinity.misc.potion.GlowingPotion;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.statuseffects.AffinityStatusEffect;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;

public class MixinHooks {

    private static final Identifier IMPENDING_DOOM_ID = Affinity.id("impending_doom");

    public static boolean textObfuscation = false;
    public static boolean injectAssemblyAugmentScreen = false;
    public static boolean forceBlockEntityRendering = false;
    public static double extraTargetingMargin = 0;
    public static @Nullable BlockEntity queuedBlockEntity = null;
    public static @Nullable EntityReference<ItemEntity> renderItem = null;

    public static final DamageTypeKey THREW_DOOM_POTION_DAMAGE = new DamageTypeKey(Affinity.id("threw_doom_potion"), DamageTypeKey.Attribution.NEVER_ATTRIBUTE);

    public static final ThreadLocal<ItemStack> POTION_UTIL_STACK = new ThreadLocal<>();
    public static final ThreadLocal<PotionUtilData> POTION_UTIL_DATA = new ThreadLocal<>();

    public static float getExtraAttackDamage(LivingEntity attacker, Entity entity, float baseAmount) {
        if (!(entity instanceof LivingEntity target)) return baseAmount;

        float extraDamage = 0;

        final var enchantments = EnchantmentHelper.get(attacker.getMainHandStack());
        for (var enchantment : enchantments.keySet()) {
            if (!(enchantment instanceof AffinityDamageEnchantment damageEnchantment)) continue;

            final int level = enchantments.get(enchantment);
            if (!damageEnchantment.shouldApplyDamage(level, attacker, target, baseAmount)) continue;

            extraDamage += damageEnchantment.getExtraDamage(level, attacker, target, baseAmount);
        }

        if (AffinityEntityAddon.getData(attacker, BerserkerEnchantment.BERSERK_KEY)) {
            extraDamage += (1 - (attacker.getHealth() / attacker.getMaxHealth())) * (1 - (attacker.getHealth() / attacker.getMaxHealth())) * 15;
        }

        return baseAmount + extraDamage;
    }

    public static boolean isDoomPotion(ItemStack stack) {
        return PotionUtil.getPotion(stack) == Registries.POTION.get(IMPENDING_DOOM_ID);
    }

    public static void potionApplied(StatusEffectInstance effect, LivingEntity target, @Nullable NbtCompound data) {
        if (effect.getEffectType() == StatusEffects.GLOWING && data != null && data.has(GlowingPotion.COLOR_KEY)) {
            target.getComponent(AffinityComponents.GLOWING_COLOR).setColor(data.get(GlowingPotion.COLOR_KEY));
        }

        if (effect.getEffectType() instanceof AffinityStatusEffect affinityEffect) {
            affinityEffect.onPotionApplied(target, data);
        }
    }

    public static boolean isMistInfusion(ItemStack ingredient, @Nullable ItemStack potionInput) {
        var potionMatches = true;
        if (potionInput != null) {
            var effects = PotionUtil.getPotionEffects(potionInput);
            potionMatches = !effects.isEmpty() && effects.stream().allMatch(effect -> effect.getAmplifier() == 1);
        }

        return potionMatches && ingredient.getItem() instanceof WispMistItem;
    }

    public static ItemStack craftMistInfusion(ItemStack ingredient, ItemStack potionInput) {
        var result = potionInput.copy();
        var effects = PotionUtil.getPotionEffects(result);

        result.setCustomName(Text.translatable("item.affinity.misty_potion").formatted(Rarity.UNCOMMON.formatting));
        result.getOrCreateNbt().putInt(PotionUtil.CUSTOM_POTION_COLOR_KEY, ((WispMistItem)ingredient.getItem()).type().color());

        PotionUtil.setPotion(result, Potions.WATER);
        PotionUtil.setCustomPotionEffects(result, effects.stream().map(instance -> new StatusEffectInstance(
                instance.getEffectType(),
                instance.getDuration(),
                instance.getAmplifier() + 1,
                instance.isAmbient(),
                instance.shouldShowParticles(),
                instance.shouldShowIcon()
        )).toList());

        return result;
    }

    public record PotionUtilData(StatusEffectInstance effectInst, float durationMultiplier) {}
}
