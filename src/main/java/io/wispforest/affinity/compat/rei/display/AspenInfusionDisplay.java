package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.AspenInfusionRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.ArrayList;
import java.util.List;

public class AspenInfusionDisplay implements Display {

    public final AspenInfusionRecipe recipe;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public AspenInfusionDisplay(AspenInfusionRecipe recipe) {
        this.recipe = recipe;

        this.inputs = new ArrayList<>(recipe.getIngredients().stream().map(EntryIngredients::ofIngredient).toList());
        this.inputs.add(0, EntryIngredients.ofIngredient(recipe.primaryInput));

        this.outputs = List.of(EntryIngredients.of(recipe.getOutput()));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return this.inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return this.outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ASPEN_INFUSION;
    }
}
