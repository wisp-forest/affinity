package com.glisco.nidween.registries;

import com.glisco.nidween.Nidween;
import com.glisco.nidween.statuseffects.FlightStatusEffect;
import com.glisco.nidween.statuseffects.NidweenStatusEffect;
import com.glisco.nidween.util.potion.PotionMixingRecipeRegistry;
import com.glisco.nidween.util.potion.PotionMixture;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.potion.Potion;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;

public class NidweenStatusEffects {

    private static final HashMap<Identifier, Potion> POTIONS = new HashMap<>();

    public static final StatusEffect LIFE_LEECH = new NidweenStatusEffect(StatusEffectType.BENEFICIAL, 0xFF0000);
    public static final StatusEffect STEADFAST = new NidweenStatusEffect(StatusEffectType.BENEFICIAL, 0x222222).addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "ea838142-499a-4460-b92c-a12d1e329b77", 1, EntityAttributeModifier.Operation.ADDITION);
    public static final StatusEffect FLIGHT = new FlightStatusEffect(StatusEffectType.BENEFICIAL, 0x6666FF);

    public static void register() {
        registerEffectAndPotions(LIFE_LEECH, "life_leech", 2400, true, true);
        registerEffectAndPotions(STEADFAST, "steadfast", 4800, true, true);

        registerEffectAndPotions(FLIGHT, "flight", 2400, true, false);

        Registry.register(Registry.POTION, Nidween.id("dubious"), PotionMixture.DUBIOUS_POTION);

        PotionMixingRecipeRegistry.registerDefaultRecipes();
    }

    private static void registerEffectAndPotions(StatusEffect effect, String baseName, int baseDuration, boolean registerLong, boolean registerStrong) {
        Registry.register(Registry.STATUS_EFFECT, Nidween.id(baseName), effect);
        POTIONS.put(Nidween.id(baseName), Registry.register(Registry.POTION, Nidween.id(baseName), new Potion(baseName, new StatusEffectInstance(effect, baseDuration))));

        if (registerLong)
            POTIONS.put(Nidween.id("long" + baseName), Registry.register(Registry.POTION, Nidween.id("long" + baseName), new Potion(baseName, new StatusEffectInstance(effect, baseDuration * 2))));
        if (registerStrong)
            POTIONS.put(Nidween.id("strong" + baseName), Registry.register(Registry.POTION, Nidween.id("strong" + baseName), new Potion(baseName, new StatusEffectInstance(effect, (int) (baseDuration * 0.5), 1))));
    }

    public static Potion getPotion(Identifier id) {
        return POTIONS.get(id);
    }

}
