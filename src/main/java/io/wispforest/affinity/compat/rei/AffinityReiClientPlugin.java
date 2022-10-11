package io.wispforest.affinity.compat.rei;

import io.wispforest.affinity.misc.recipe.PotionMixingRecipe;
import io.wispforest.affinity.misc.recipe.ShapedAssemblyRecipe;
import io.wispforest.affinity.misc.recipe.ShapelessAssemblyRecipe;
import io.wispforest.affinity.object.AffinityBlocks;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.block.Blocks;

public class AffinityReiClientPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new PotionMixingCategory());
        registry.add(new AssemblyCategory());

        registry.addWorkstations(AffinityReiCommonPlugin.POTION_MIXING, EntryStacks.of(AffinityBlocks.BREWING_CAULDRON));
        registry.addWorkstations(AffinityReiCommonPlugin.POTION_MIXING, EntryStacks.of(Blocks.SPORE_BLOSSOM));

        registry.addWorkstations(AffinityReiCommonPlugin.ASSEMBLY, EntryStacks.of(Blocks.CRAFTING_TABLE), EntryStacks.of(AffinityBlocks.ASSEMBLY_AUGMENT));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(PotionMixingRecipe.class, PotionMixingDisplay::new);

        registry.registerFiller(ShapedAssemblyRecipe.class, ShapedAssemblyDisplay::new);
        registry.registerFiller(ShapelessAssemblyRecipe.class, ShapelessAssemblyDisplay::new);
    }

}
