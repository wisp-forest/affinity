package io.wispforest.affinity.compat.rei;

import io.wispforest.affinity.block.impl.ArcaneFadeBlock;
import io.wispforest.affinity.compat.rei.category.*;
import io.wispforest.affinity.compat.rei.display.*;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.recipe.AspenInfusionRecipe;
import io.wispforest.affinity.recipe.PotionMixingRecipe;
import io.wispforest.affinity.recipe.ShapedAssemblyRecipe;
import io.wispforest.affinity.recipe.ShapelessAssemblyRecipe;
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
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AffinityReiClientPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new PotionMixingCategory());
        registry.add(new AssemblyCategory());
        registry.add(new ContainingPotionsCategory());
        registry.add(new AspenInfusionCategory());
        registry.add(new ArcaneFadingCategory());

        registry.addWorkstations(AffinityReiCommonPlugin.POTION_MIXING, EntryStacks.of(Blocks.SPORE_BLOSSOM));
        registry.addWorkstations(AffinityReiCommonPlugin.POTION_MIXING, EntryStacks.of(AffinityBlocks.BREWING_CAULDRON));

        registry.addWorkstations(AffinityReiCommonPlugin.ASPEN_INFUSION, EntryStacks.of(AffinityBlocks.ASP_RITE_CORE));

        registry.addWorkstations(AffinityReiCommonPlugin.ASSEMBLY, EntryStacks.of(AffinityBlocks.ASSEMBLY_AUGMENT), EntryStacks.of(Blocks.CRAFTING_TABLE));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(PotionMixingRecipe.class, PotionMixingDisplay::new);
        registry.registerFiller(AspenInfusionRecipe.class, AspenInfusionDisplay::new);

        registry.registerFiller(ShapedAssemblyRecipe.class, ShapedAssemblyDisplay::new);
        registry.registerFiller(ShapelessAssemblyRecipe.class, ShapelessAssemblyDisplay::new);

        ArcaneFadeBlock.forEachGroup((item, items) -> {
            registry.add(new ArcaneFadingDisplay(items, item));
        });

        var effectToPotion = new HashMap<StatusEffect, List<Potion>>();

        for (Potion potion : Registries.POTION) {
            for (StatusEffectInstance effectInst : potion.getEffects()) {
                effectToPotion.computeIfAbsent(effectInst.getEffectType(), unused -> new ArrayList<>()).add(potion);
            }
        }

        effectToPotion.forEach((key, value) -> registry.add(new ContainingPotionsDisplay(key, value)));
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        for (StatusEffect effect : Registries.STATUS_EFFECT) {
            registry.addEntry(EntryStack.of(AffinityReiCommonPlugin.EFFECT_ENTRY_TYPE, effect));
        }
    }

}
