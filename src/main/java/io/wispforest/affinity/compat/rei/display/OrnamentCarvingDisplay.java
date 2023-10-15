package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.OrnamentCarvingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.recipe.RecipeEntry;

import java.util.List;
import java.util.Optional;

public class OrnamentCarvingDisplay extends BasicDisplay {

    public OrnamentCarvingDisplay(RecipeEntry<OrnamentCarvingRecipe> recipeEntry) {
        super(List.of(EntryIngredients.ofIngredient(recipeEntry.value().input)), List.of(EntryIngredients.of(recipeEntry.value().getResult(null))), Optional.of(recipeEntry.id()));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ORNAMENT_CARVING;
    }
}
