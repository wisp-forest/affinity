package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.PotionMixingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PotionMixingDisplay extends BasicDisplay {

    public final PotionMixingRecipe recipe;

    public PotionMixingDisplay(RecipeEntry<PotionMixingRecipe> recipeEntry) {
        super(
                Stream.concat(
                        recipeEntry.value().itemInputs.stream().map(EntryIngredients::ofIngredient),
                        recipeEntry.value().effectInputs.stream().map(effect -> EntryIngredients.of(AffinityReiCommonPlugin.EFFECT_ENTRY_TYPE, List.of(effect)))
                ).toList(),
                Util.make(() -> {
                    final var potionStack = new ItemStack(Items.POTION);
                    PotionUtil.setPotion(potionStack, recipeEntry.value().potionOutput());
                    return List.of(EntryIngredients.of(potionStack));
                }),
                Optional.of(recipeEntry.id())
        );

        this.recipe = recipeEntry.value();
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.POTION_MIXING;
    }
}
