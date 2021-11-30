package com.glisco.nidween.compat.rei;

import com.glisco.nidween.registries.NidweenBlocks;
import com.glisco.nidween.util.recipe.PotionMixingRecipe;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.block.Blocks;

public class NidweenReiPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {

        registry.add(new PotionMixingCategory());

        registry.addWorkstations(PotionMixingCategory.ID, EntryStacks.of(NidweenBlocks.BREWING_CAULDRON));
        registry.addWorkstations(PotionMixingCategory.ID, EntryStacks.of(Blocks.SPORE_BLOSSOM));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(PotionMixingRecipe.class, PotionMixingDisplay::new);
    }


}
