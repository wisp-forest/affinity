package io.wispforest.affinity.compat.rei;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.architectury.event.EventResult;
import io.wispforest.affinity.Affinity;
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
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.*;

public class AffinityReiClientPlugin implements REIClientPlugin {

    private static final Set<Identifier> HIDDEN_RECIPES = new HashSet<>();

    static {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new IgnoredRecipesLoader());
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new PotionMixingCategory());
        registry.addWorkstations(AffinityReiCommonPlugin.POTION_MIXING, EntryStacks.of(Blocks.SPORE_BLOSSOM), EntryStacks.of(AffinityBlocks.BREWING_CAULDRON));

        registry.add(new AssemblyCategory());
        registry.addWorkstations(AffinityReiCommonPlugin.ASSEMBLY, EntryStacks.of(AffinityBlocks.ASSEMBLY_AUGMENT), EntryStacks.of(Blocks.CRAFTING_TABLE));

        registry.add(new AspenInfusionCategory());
        registry.add(new SpiritAssimilationCategory());
        registry.addWorkstations(AffinityReiCommonPlugin.ASPEN_INFUSION, EntryStacks.of(AffinityBlocks.ASP_RITE_CORE));
        registry.addWorkstations(AffinityReiCommonPlugin.SPIRIT_ASSIMILATION, EntryStacks.of(AffinityBlocks.SPIRIT_INTEGRATION_APPARATUS));
        for (var item : Registries.ITEM.iterateEntries(SpiritAssimilationCategory.RECIPE_RITUAL_SOCLE_PREVIEW)) {
            registry.addWorkstations(AffinityReiCommonPlugin.ASPEN_INFUSION, EntryIngredients.of(item.value()));
            registry.addWorkstations(AffinityReiCommonPlugin.SPIRIT_ASSIMILATION, EntryIngredients.of(item.value()));
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
        registry.registerFiller(SpiritAssimilationRecipe.class, SpiritAssimilationDisplay::new);
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

        HIDDEN_RECIPES.forEach(identifier -> {
            registry.registerVisibilityPredicate((category, display) -> {
                return display.getDisplayLocation().map(HIDDEN_RECIPES::contains).orElse(false)
                        ? EventResult.interruptFalse()
                        : EventResult.pass();
            });
        });
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

    private static class IgnoredRecipesLoader implements SynchronousResourceReloader, IdentifiableResourceReloadListener {

        private static final Gson GSON = new GsonBuilder().create();

        @Override
        public Identifier getFabricId() {
            return Affinity.id("rei_hidden_recipes");
        }

        @Override
        public void reload(ResourceManager manager) {
            HIDDEN_RECIPES.clear();

            manager.findResources("rei_hidden_recipes", identifier -> identifier.getPath().endsWith(".json")).forEach((identifier, resource) -> {
                try {
                    var json = GSON.fromJson(resource.getReader(), JsonObject.class);

                    var recipeArray = JsonHelper.getArray(json, "hidden_recipes");
                    for (var element : recipeArray) {
                        HIDDEN_RECIPES.add(new Identifier(element.getAsString()));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Exception loading hidden REI recipes from " + identifier, e);
                }
            });
        }
    }
}
