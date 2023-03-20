package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.OrnamentCarvingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;
import java.util.Optional;

public class OrnamentCarvingDisplay extends BasicDisplay {

    public OrnamentCarvingDisplay(OrnamentCarvingRecipe recipe) {
        super(List.of(EntryIngredients.ofIngredient(recipe.input)), List.of(EntryIngredients.of(recipe.getOutput())), Optional.of(recipe.getId()));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ORNAMENT_CARVING;
    }
}
