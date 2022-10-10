package io.wispforest.affinity.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay;
import net.minecraft.recipe.ShapedRecipe;

public class ShapedAssemblyDisplay extends DefaultShapedDisplay {
    public ShapedAssemblyDisplay(ShapedRecipe recipe) {
        super(recipe);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ASSEMBLY;
    }
}
