package io.wispforest.affinity.compat.rei;

import io.wispforest.affinity.misc.recipe.PotionMixingRecipe;
import io.wispforest.affinity.object.AffinityBlocks;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.block.Blocks;

public class AffinityReiPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {

        registry.add(new PotionMixingCategory());

        registry.addWorkstations(PotionMixingCategory.ID, EntryStacks.of(AffinityBlocks.BREWING_CAULDRON));
        registry.addWorkstations(PotionMixingCategory.ID, EntryStacks.of(Blocks.SPORE_BLOSSOM));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(PotionMixingRecipe.class, PotionMixingDisplay::new);
    }


}
