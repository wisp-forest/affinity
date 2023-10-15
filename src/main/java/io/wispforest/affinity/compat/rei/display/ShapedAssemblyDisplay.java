package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.ShapedAssemblyRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;

public class ShapedAssemblyDisplay extends DefaultShapedDisplay {

    @SuppressWarnings("unchecked")
    public ShapedAssemblyDisplay(RecipeEntry<ShapedAssemblyRecipe> recipe) {
        super((RecipeEntry<ShapedRecipe>) (Object) recipe);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ASSEMBLY;
    }
}
