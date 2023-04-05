package io.wispforest.affinity.misc;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.enchantment.impl.BerserkerEnchantment;
import io.wispforest.affinity.enchantment.template.AffinityDamageEnchantment;
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
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class MixinHooks {

    private static final Identifier IMPENDING_DOOM_ID = Affinity.id("impending_doom");

    public static boolean TEXT_OBFUSCATION = false;
    public static boolean INJECT_ASSEMBLY_AUGMENT_SCREEN = false;
    public static boolean FORCE_BLOCK_ENTITY_RENDERING = false;
    public static double EXTRA_TARGETING_MARGIN = 0;
    public static @Nullable BlockEntity QUEUED_BLOCKENTITY = null;
    public static @Nullable EntityReference<ItemEntity> RENDER_ITEM = null;

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
            AffinityComponents.GLOWING_COLOR.get(target).setColor(data.get(GlowingPotion.COLOR_KEY));
        }

        if (effect.getEffectType() instanceof AffinityStatusEffect affinityEffect) {
            affinityEffect.onPotionApplied(target, data);
        }
    }

    public record PotionUtilData(StatusEffectInstance effectInst, float durationMultiplier) {}
}
