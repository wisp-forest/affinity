package com.glisco.nidween.util.potion;

import com.glisco.nidween.Nidween;
import com.glisco.nidween.registries.NidweenStatusEffects;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PotionMixingRecipeRegistry {

    private static final List<PotionMixingRecipe> recipes = new ArrayList<>();

    public static void registerDefaultRecipes() {
        registerRecipe(new PotionMixingRecipe(ImmutableList.of(StatusEffects.JUMP_BOOST, StatusEffects.SLOW_FALLING), ImmutableList.of(), NidweenStatusEffects.getPotion(Nidween.id("flight"))));
    }

    public static void registerRecipe(PotionMixingRecipe recipe) {
        recipes.add(recipe);
    }

    public static Optional<PotionMixingRecipe> getOrEmpty(PotionMixture mixture, List<ItemStack> itemInputs) {
        return recipes.stream().filter(recipe -> recipe.matches(mixture, itemInputs)).findAny();
    }

}
