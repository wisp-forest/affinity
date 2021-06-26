package com.glisco.nidween.util.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Ingredient;

import java.util.List;

public record PotionMixingRecipe(List<StatusEffect> potionInputs, List<Ingredient> itemInputs, Potion output) {

    public boolean matches(PotionMixture mixture, List<ItemStack> stacks) {
        final var effects = mixture.getCustomEffects().stream().map(StatusEffectInstance::getEffectType).toList();

        return effects.containsAll(potionInputs) && itemInputs.stream().allMatch(ingredient -> {
            for (var stack : stacks) {
                if (ingredient.test(stack)) return true;
            }
            return false;
        });
    }

}
