package io.wispforest.affinity.compat.emi.recipe;

import com.google.common.collect.Lists;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

import java.util.List;

public class AssemblyEmiRecipe extends EmiCraftingRecipe {
    public AssemblyEmiRecipe(List<EmiIngredient> input, EmiStack output, Identifier id, boolean shapeless) {
        super(input, output, id, shapeless);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        super.addWidgets(widgets);
        widgets.addTexture(Affinity.id("textures/gui/assembly_augment.png"), widgets.getWidth() - 9, (widgets.getHeight() - 34) / 2, 6, 34, 176, 83);
    }

    @Override
    public int getDisplayWidth() {
        return super.getDisplayWidth() + 14;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return AffinityEmiPlugin.ASSEMBLY;
    }

    public static List<EmiIngredient> padShapedIngredients(ShapedRecipe recipe) {
        List<EmiIngredient> list = Lists.newArrayList();
        int i = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x >= recipe.getWidth() || y >= recipe.getHeight() || i >= recipe.getIngredients().size()) {
                    list.add(EmiStack.EMPTY);
                } else {
                    list.add(AffinityEmiPlugin.veryCoolFeatureYouGotThereEmi(recipe.getIngredients().get(i++)));
                }
            }
        }
        return list;
    }
}
