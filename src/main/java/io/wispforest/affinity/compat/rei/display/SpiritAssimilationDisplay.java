package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.SpiritAssimilationRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpiritAssimilationDisplay extends BasicDisplay {

    public final SpiritAssimilationRecipe recipe;

    public SpiritAssimilationDisplay(SpiritAssimilationRecipe recipe) {
        super(Util.make(() -> {
            var inputs = new ArrayList<>(recipe.coreInputs.stream().map(EntryIngredients::ofIngredient).toList());
            inputs.addAll(recipe.getIngredients().stream().map(EntryIngredients::ofIngredient).toList());
            return inputs;
        }), List.of(EntryIngredients.of(recipe.getOutput())), Optional.of(recipe.getId()));

        this.recipe = recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.SPIRIT_ASSIMILATION;
    }
}
