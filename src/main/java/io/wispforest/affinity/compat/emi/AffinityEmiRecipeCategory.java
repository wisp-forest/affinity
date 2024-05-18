package io.wispforest.affinity.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class AffinityEmiRecipeCategory extends EmiRecipeCategory {
    public AffinityEmiRecipeCategory(Identifier id, EmiRenderable icon) {
        super(id, icon);
    }

    @Override
    public Text getName() {
        return Text.translatable(Util.createTranslationKey("category", this.id));
    }
}
