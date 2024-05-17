package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import net.minecraft.util.Identifier;

import java.util.List;

public class AssemblyEmiRecipe extends EmiCraftingRecipe {
    public AssemblyEmiRecipe(List<EmiIngredient> input, EmiStack output, Identifier id, boolean shapeless) {
        super(input, output, id, shapeless);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return AffinityEmiPlugin.ASSEMBLY;
    }
}
