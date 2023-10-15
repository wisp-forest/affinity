package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.ShapelessAssemblyRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapelessDisplay;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapelessRecipe;

public class ShapelessAssemblyDisplay extends DefaultShapelessDisplay {

    @SuppressWarnings("unchecked")
    public ShapelessAssemblyDisplay(RecipeEntry<ShapelessAssemblyRecipe> recipe) {
        super((RecipeEntry<ShapelessRecipe>) (Object) recipe);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ASSEMBLY;
    }
}
