package io.wispforest.affinity.compat.rei;

import io.wispforest.affinity.block.impl.ArcaneFadeBlock;
import io.wispforest.affinity.client.screen.AssemblyAugmentScreen;
import io.wispforest.affinity.compat.rei.category.*;
import io.wispforest.affinity.compat.rei.display.*;
import io.wispforest.affinity.item.SocleOrnamentItem;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.recipe.*;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
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
        registry.addWorkstations(AffinityReiCommonPlugin.POTION_MIXING, EntryStacks.of(Blocks.SPORE_BLOSSOM), EntryStacks.of(AffinityBlocks.BREWING_CAULDRON));

        registry.add(new AssemblyCategory());
        registry.addWorkstations(AffinityReiCommonPlugin.ASSEMBLY, EntryStacks.of(AffinityBlocks.ASSEMBLY_AUGMENT), EntryStacks.of(Blocks.CRAFTING_TABLE));

        registry.add(new AspenInfusionCategory());
        registry.add(new AberrantCallingCategory());
        registry.addWorkstations(AffinityReiCommonPlugin.ASPEN_INFUSION, EntryStacks.of(AffinityBlocks.ASP_RITE_CORE));
        registry.addWorkstations(AffinityReiCommonPlugin.ABERRANT_CALLING, EntryStacks.of(AffinityBlocks.ABERRANT_CALLING_CORE));
        for (var item : Registries.ITEM.iterateEntries(AberrantCallingCategory.RECIPE_RITUAL_SOCLE_PREVIEW)) {
            registry.addWorkstations(AffinityReiCommonPlugin.ASPEN_INFUSION, EntryIngredients.of(item.value()));
            registry.addWorkstations(AffinityReiCommonPlugin.ABERRANT_CALLING, EntryIngredients.of(item.value()));
        }

        registry.add(new ArcaneFadingCategory());
        registry.addWorkstations(AffinityReiCommonPlugin.ARCANE_FADING, EntryStacks.of(AffinityItems.ARCANE_FADE_BUCKET));

        registry.add(new OrnamentCarvingCategory());
        registry.add(new SocleComposingCategory());
        registry.addWorkstations(AffinityReiCommonPlugin.ORNAMENT_CARVING, EntryStacks.of(AffinityBlocks.RITUAL_SOCLE_COMPOSER));
        registry.addWorkstations(AffinityReiCommonPlugin.SOCLE_COMPOSING, EntryStacks.of(AffinityBlocks.RITUAL_SOCLE_COMPOSER));

        registry.add(new ContainingPotionsCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(PotionMixingRecipe.class, PotionMixingDisplay::new);
        registry.registerFiller(AspenInfusionRecipe.class, AspenInfusionDisplay::new);
        registry.registerFiller(AberrantCallingRecipe.class, AberrantCallingDisplay::new);
        registry.registerFiller(OrnamentCarvingRecipe.class, OrnamentCarvingDisplay::new);

        registry.registerFiller(ShapedAssemblyRecipe.class, ShapedAssemblyDisplay::new);
        registry.registerFiller(ShapelessAssemblyRecipe.class, ShapelessAssemblyDisplay::new);

        Registries.ITEM.stream()
                .filter(SocleOrnamentItem.class::isInstance)
                .map(SocleOrnamentItem.class::cast)
                .map(SocleOrnamentItem::socleType)
                .forEach(type -> {
                    registry.add(new SocleComposingDisplay(type, SocleComposingDisplay.Action.CRAFT));
                    registry.add(new SocleComposingDisplay(type, SocleComposingDisplay.Action.UNCRAFT));
                });

        ArcaneFadeBlock.forEachGroup((id, item, items) -> {
            registry.add(new ArcaneFadingDisplay(items, item, id));
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

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerClickArea(screen -> {
            return new Rectangle(screen.rootX() + 90, screen.rootY() + 35, 22, 15);
        }, AssemblyAugmentScreen.class, AffinityReiCommonPlugin.ASSEMBLY);
    }
}
