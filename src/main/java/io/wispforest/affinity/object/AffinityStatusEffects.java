package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.potion.GlowingPotion;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.statuseffects.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.function.BiFunction;

// TODO: migrate to RegistryEntry
public class AffinityStatusEffects {

    public static final StatusEffect LIFE_LEECH = new AffinityStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000);
    public static final StatusEffect FLIGHT = new FlightStatusEffect(StatusEffectCategory.BENEFICIAL, 0x6666FF);
    public static final StatusEffect FREEZING = new FreezingStatusEffect(StatusEffectCategory.HARMFUL, 0x000066);
    public static final StatusEffect DRIPPING = new DrippingStatusEffect(StatusEffectCategory.NEUTRAL, 0xa4568d);
    public static final StatusEffect IMPENDING_DOOM = new ImpendingDoomStatusEffect(StatusEffectCategory.HARMFUL, 0x000000);
    public static final StatusEffect BASTION_REGENERATION = new BastionRegenerationStatusEffect(StatusEffectCategory.BENEFICIAL, 0xfd5c5b);
    public static final StatusEffect BANISHED = new BanishedStatusEffect(StatusEffectCategory.BENEFICIAL, 0xc9b6b3);
    public static final StatusEffect AFFINE = new AffinityStatusEffect(StatusEffectCategory.BENEFICIAL, 0x6137d6);
    public static final StatusEffect UNHOLY = new AffinityStatusEffect(StatusEffectCategory.HARMFUL, 0x000000);
    public static final StatusEffect RESONANT = new ResonantStatusEffect(StatusEffectCategory.HARMFUL, 0xffec4f);
    public static final StatusEffect CAT_ANXIETY = new AffinityStatusEffect(StatusEffectCategory.BENEFICIAL, 0x00ff00);

    public static final StatusEffect STEADFAST = new AffinityStatusEffect(StatusEffectCategory.BENEFICIAL, 0x222222)
            .addAttributeModifier(
                    EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                    Affinity.id("steadfast_knockback_resistance"),
                    1,
                    EntityAttributeModifier.Operation.ADD_VALUE
            );

    public static void register() {
        registerEffectAndPotions(LIFE_LEECH, "life_leech", 2400, true, true);
        registerEffectAndPotions(STEADFAST, "steadfast", 4800, true, true);
        registerEffectAndPotions(FREEZING, "freezing", 600, true, true);
        registerEffectAndPotions(IMPENDING_DOOM, "impending_doom", 1200, false, false);
        registerEffectAndPotions(BANISHED, "banished", 6000, true, false);
        registerEffectAndPotions(UNHOLY, "unholy", 300, true, true);
        registerEffectAndPotions(CAT_ANXIETY, "cat_anxiety", 6900, true, false);

        registerEffectAndPotions(FLIGHT, "flight", 2400, true, false);

        registerPotions(StatusEffects.RESISTANCE.value(), "resistance", 4800, true, true);
        registerPotions(StatusEffects.GLOWING.value(), "glowing", 9600, true, false, GlowingPotion::new);
        registerPotions(StatusEffects.WITHER.value(), "wither", 400, true, true);
        registerPotions(StatusEffects.HUNGER.value(), "hunger", 1200, true, true);
        registerPotions(StatusEffects.LEVITATION.value(), "levitation", 600, true, true);
        registerPotions(StatusEffects.BLINDNESS.value(), "blindness", 300, true, false);

        Registry.register(Registries.STATUS_EFFECT, Affinity.id("dripping"), DRIPPING);
        Registry.register(Registries.STATUS_EFFECT, Affinity.id("bastion_regeneration"), BASTION_REGENERATION);
        Registry.register(Registries.STATUS_EFFECT, Affinity.id("affine"), AFFINE);
        Registry.register(Registries.STATUS_EFFECT, Affinity.id("resonant"), RESONANT);

        Registry.register(Registries.POTION, Affinity.id("dubious"), PotionMixture.DUBIOUS_POTION);
    }

    private static void registerEffectAndPotions(StatusEffect effect, String baseName, int baseDuration, boolean registerLong, boolean registerStrong) {
        Registry.register(Registries.STATUS_EFFECT, Affinity.id(baseName), effect);
        registerPotions(effect, baseName, baseDuration, registerLong, registerStrong);
    }

    private static void registerEffectAndPotions(StatusEffect effect, String baseName, int baseDuration, boolean registerLong, boolean registerStrong, BiFunction<String, StatusEffectInstance, Potion> potionCreator) {
        Registry.register(Registries.STATUS_EFFECT, Affinity.id(baseName), effect);
        registerPotions(effect, baseName, baseDuration, registerLong, registerStrong, potionCreator);
    }

    private static void registerPotions(StatusEffect effect, String baseName, int baseDuration, boolean registerLong, boolean registerStrong) {
        registerPotions(effect, baseName, baseDuration, registerLong, registerStrong, Potion::new);
    }

    private static void registerPotions(StatusEffect effect, String baseName, int baseDuration, boolean registerLong, boolean registerStrong, BiFunction<String, StatusEffectInstance, Potion> potionFactory) {
        RegistryEntry<StatusEffect> effectEntry = Registries.STATUS_EFFECT.getEntry(effect);

        Registry.register(Registries.POTION, Affinity.id(baseName), potionFactory.apply(baseName, new StatusEffectInstance(effectEntry, baseDuration)));

        if (registerLong) {
            Registry.register(Registries.POTION, Affinity.id("long_" + baseName), potionFactory.apply(baseName, new StatusEffectInstance(effectEntry, baseDuration * 2)));
        }
        if (registerStrong) {
            Registry.register(Registries.POTION, Affinity.id("strong_" + baseName), potionFactory.apply(baseName, new StatusEffectInstance(effectEntry, (int) (baseDuration * 0.5), 1)));
        }
    }

}
