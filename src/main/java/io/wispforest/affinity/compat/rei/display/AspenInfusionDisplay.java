package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.AspenInfusionRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AspenInfusionDisplay extends BasicDisplay {

    public final AspenInfusionRecipe recipe;

    public AspenInfusionDisplay(RecipeEntry<AspenInfusionRecipe> recipeEntry) {
        super(Util.make(() -> {
            var inputs = new ArrayList<>(recipeEntry.value().getIngredients().stream().map(EntryIngredients::ofIngredient).toList());
            inputs.add(0, EntryIngredients.ofIngredient(recipeEntry.value().primaryInput));
            return inputs;
        }), List.of(EntryIngredients.of(recipeEntry.value().getResult(null))), Optional.of(recipeEntry.id()));

        this.recipe = recipeEntry.value();
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ASPEN_INFUSION;
    }
}
