package io.wispforest.affinity.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.plugin.client.categories.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.text.Text;

public class AssemblyCategory extends DefaultCraftingCategory {
    @Override
    public Text getTitle() {
        return Text.translatable("category.affinity.assembly");
    }

    @Override
    public CategoryIdentifier<? extends DefaultCraftingDisplay<?>> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ASSEMBLY;
    }
}
