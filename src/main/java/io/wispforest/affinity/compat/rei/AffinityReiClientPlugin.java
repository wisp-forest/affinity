package io.wispforest.affinity.compat.rei;

import io.wispforest.affinity.misc.recipe.PotionMixingRecipe;
import io.wispforest.affinity.misc.recipe.ShapedAssemblyRecipe;
import io.wispforest.affinity.misc.recipe.ShapelessAssemblyRecipe;
import io.wispforest.affinity.object.AffinityBlocks;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AffinityReiClientPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new PotionMixingCategory());
        registry.add(new AssemblyCategory());
        registry.add(new ContainedPotionsCategory());

        registry.addWorkstations(AffinityReiCommonPlugin.POTION_MIXING, EntryStacks.of(AffinityBlocks.BREWING_CAULDRON));
        registry.addWorkstations(AffinityReiCommonPlugin.POTION_MIXING, EntryStacks.of(Blocks.SPORE_BLOSSOM));

        registry.addWorkstations(AffinityReiCommonPlugin.ASSEMBLY, EntryStacks.of(Blocks.CRAFTING_TABLE), EntryStacks.of(AffinityBlocks.ASSEMBLY_AUGMENT));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(PotionMixingRecipe.class, PotionMixingDisplay::new);

        registry.registerFiller(ShapedAssemblyRecipe.class, ShapedAssemblyDisplay::new);
        registry.registerFiller(ShapelessAssemblyRecipe.class, ShapelessAssemblyDisplay::new);

        Map<StatusEffect, List<Potion>> effectToPotion = new HashMap<>();

        for (Potion potion : Registry.POTION) {
            for (StatusEffectInstance effectInst : potion.getEffects()) {
                effectToPotion.computeIfAbsent(effectInst.getEffectType(), unused -> new ArrayList<>()).add(potion);
            }
        }

        for (Map.Entry<StatusEffect, List<Potion>> entry : effectToPotion.entrySet()) {
            registry.add(new ContainedPotionsDisplay(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        for (StatusEffect effect : Registry.STATUS_EFFECT) {
            registry.addEntry(EntryStack.of(AffinityReiCommonPlugin.EFFECT_ENTRY_TYPE, effect));
        }
    }

}
