package io.wispforest.affinity.misc;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.enchantment.impl.BerserkerEnchantment;
import io.wispforest.affinity.enchantment.template.AffinityDamageEnchantment;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.statuseffects.AffinityStatusEffect;
import io.wispforest.affinity.statuseffects.ImpendingDoomStatusEffect;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MixinHooks {

    private static final Identifier IMPENDING_DOOM_ID = Affinity.id("impending_doom");

    public static boolean TEXT_OBFUSCATION = false;
    public static final DamageSource THREW_DOOM_POTION_SOURCE = new ImpendingDoomStatusEffect.DoomDamageSource("threw_doom_potion");

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
            attacker.sendSystemMessage(Text.of("Extra damage: " + extraDamage), null);
        }

        return baseAmount + extraDamage;
    }

    public static boolean isDoomPotion(ItemStack stack) {
        return PotionUtil.getPotion(stack) == Registry.POTION.get(IMPENDING_DOOM_ID);
    }

    public static void tryInvokePotionApplied(StatusEffectInstance effect, LivingEntity target, NbtCompound data) {
        if (effect.getEffectType() instanceof AffinityStatusEffect ase) {
            ase.onPotionApplied(target, data);
        }
    }

}
