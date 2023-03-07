package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.AberrantCallingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.ArrayList;
import java.util.List;

public class AberrantCallingDisplay implements Display {

    public final AberrantCallingRecipe recipe;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public AberrantCallingDisplay(AberrantCallingRecipe recipe) {
        this.recipe = recipe;

        this.inputs = new ArrayList<>(recipe.coreInputs.stream().map(EntryIngredients::ofIngredient).toList());
        this.inputs.addAll(recipe.getIngredients().stream().map(EntryIngredients::ofIngredient).toList());

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
        return AffinityReiCommonPlugin.ABERRANT_CALLING;
    }
}
