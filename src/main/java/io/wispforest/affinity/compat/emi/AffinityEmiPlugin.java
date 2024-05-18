package io.wispforest.affinity.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.ArcaneFadeBlock;
import io.wispforest.affinity.compat.emi.recipe.*;
import io.wispforest.affinity.compat.rei.display.SocleComposingDisplay;
import io.wispforest.affinity.item.SocleOrnamentItem;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.affinity.recipe.ShapedAssemblyRecipe;
import io.wispforest.affinity.recipe.ShapelessAssemblyRecipe;
import io.wispforest.affinity.recipe.ingredient.PotionIngredient;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AffinityEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory ASPEN_INFUSION = new AffinityEmiRecipeCategory(Affinity.id("aspen_infusion"), EmiStack.of(AffinityBlocks.ASP_RITE_CORE));
    public static final EmiRecipeCategory POTION_MIXING = new AffinityEmiRecipeCategory(Affinity.id("potion_mixing"), EmiStack.of(AffinityBlocks.BREWING_CAULDRON));
    public static final EmiRecipeCategory CONTAINING_POTIONS = new AffinityEmiRecipeCategory(Affinity.id("containing_potions"), Util.make(() -> {
        ItemStack stack = new ItemStack(Items.POTION);
        PotionUtil.setPotion(stack, Potions.STRENGTH);
        return EmiStack.of(stack);
    }));
    public static final EmiRecipeCategory SPIRIT_ASSIMILATION = new AffinityEmiRecipeCategory(Affinity.id("spirit_assimilation"), EmiStack.of(AffinityBlocks.SPIRIT_INTEGRATION_APPARATUS));
    public static final EmiRecipeCategory ASSEMBLY = new AffinityEmiRecipeCategory(Affinity.id("assembly"), EmiStack.of(AffinityBlocks.ASSEMBLY_AUGMENT));
    public static final EmiRecipeCategory ORNAMENT_CARVING = new AffinityEmiRecipeCategory(Affinity.id("ornament_carving"), EmiStack.of(AffinityBlocks.RITUAL_SOCLE_COMPOSER));
    public static final EmiRecipeCategory SOCLE_COMPOSING = new AffinityEmiRecipeCategory(Affinity.id("socle_composing"), EmiStack.of(AffinityBlocks.RITUAL_SOCLE_COMPOSER));
    public static final EmiRecipeCategory ARCANE_FADING = new AffinityEmiRecipeCategory(Affinity.id("arcane_fading"), EmiStack.of(AffinityItems.ARCANE_FADE_BUCKET));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ASPEN_INFUSION);
        registry.addWorkstation(ASPEN_INFUSION, EmiStack.of(AffinityBlocks.ASP_RITE_CORE));

        for (var recipe : registry.getRecipeManager().listAllOfType(AffinityRecipeTypes.ASPEN_INFUSION)) {
            registry.addRecipe(new AspenInfusionEmiRecipe(recipe.id(), recipe.value()));
        }

        // ---

        for (var effect : Registries.STATUS_EFFECT) {
            registry.addEmiStack(new StatusEffectEmiStack(effect));
        }

        registry.addCategory(POTION_MIXING);
        registry.addWorkstation(POTION_MIXING, EmiStack.of(Blocks.SPORE_BLOSSOM));
        registry.addWorkstation(POTION_MIXING, EmiStack.of(AffinityBlocks.BREWING_CAULDRON));

        for (var recipe : registry.getRecipeManager().listAllOfType(AffinityRecipeTypes.POTION_MIXING)) {
            registry.addRecipe(new PotionMixingEmiRecipe(recipe.id(), recipe.value()));
        }

        // ---

        registry.addCategory(CONTAINING_POTIONS);

        var effectToPotion = new HashMap<StatusEffect, List<Potion>>();
        for (var potion : Registries.POTION) {
            for (var effectInst : potion.getEffects()) {
                effectToPotion.computeIfAbsent(effectInst.getEffectType(), unused -> new ArrayList<>()).add(potion);
            }
        }

        effectToPotion.forEach((effect, potions) -> {
            potions.forEach(potion -> registry.addRecipe(new ContainingPotionsEmiRecipe(effect, potion)));
        });

        // ---

        registry.addCategory(SPIRIT_ASSIMILATION);
        registry.addWorkstation(SPIRIT_ASSIMILATION, EmiStack.of(AffinityBlocks.SPIRIT_INTEGRATION_APPARATUS));

        for (var recipe : registry.getRecipeManager().listAllOfType(AffinityRecipeTypes.SPIRIT_ASSIMILATION)) {
            registry.addRecipe(new SpiritAssimilationEmiRecipe(recipe.id(), recipe.value()));
        }

        // ---

        registry.addCategory(ASSEMBLY);
        registry.addWorkstation(ASSEMBLY, EmiStack.of(AffinityBlocks.ASSEMBLY_AUGMENT));

        for (var recipe : registry.getRecipeManager().listAllOfType(AffinityRecipeTypes.ASSEMBLY)) {
            if (recipe.value() instanceof ShapedAssemblyRecipe shaped) {
                registry.addRecipe(new AssemblyEmiRecipe(
                        AssemblyEmiRecipe.padShapedIngredients(shaped),
                        EmiStack.of(shaped.getResult(MinecraftClient.getInstance().world.getRegistryManager())),
                        recipe.id(),
                        false
                ));
            } else if (recipe.value() instanceof ShapelessAssemblyRecipe shapeless) {
                registry.addRecipe(new AssemblyEmiRecipe(
                        shapeless.getIngredients().stream().map(AffinityEmiPlugin::veryCoolFeatureYouGotThereEmi).toList(),
                        EmiStack.of(shapeless.getResult(MinecraftClient.getInstance().world.getRegistryManager())),
                        recipe.id(),
                        true
                ));
            }
        }

        registry.addRecipeHandler(AffinityScreenHandlerTypes.ASSEMBLY_AUGMENT, new AssemblyRecipeHandler());
        registry.addRecipeHandler(AffinityScreenHandlerTypes.ASSEMBLY_AUGMENT, new AssemblyRecipeHandler());

        // ---

        registry.addCategory(ORNAMENT_CARVING);
        registry.addWorkstation(ORNAMENT_CARVING, EmiStack.of(AffinityBlocks.RITUAL_SOCLE_COMPOSER));

        for (var recipe : registry.getRecipeManager().listAllOfType(AffinityRecipeTypes.ORNAMENT_CARVING)) {
            registry.addRecipe(new OrnamentCarvingEmiRecipe(recipe.id(), recipe.value()));
        }

        registry.addCategory(SOCLE_COMPOSING);
        registry.addWorkstation(SOCLE_COMPOSING, EmiStack.of(AffinityBlocks.RITUAL_SOCLE_COMPOSER));

        Registries.ITEM.stream()
                .filter(SocleOrnamentItem.class::isInstance)
                .map(SocleOrnamentItem.class::cast)
                .map(SocleOrnamentItem::socleType)
                .forEach(type -> {
                    registry.addRecipe(new SocleComposingEmiRecipe(type, SocleComposingDisplay.Action.CRAFT));
                    registry.addRecipe(new SocleComposingEmiRecipe(type, SocleComposingDisplay.Action.UNCRAFT));
                });

        // ---

        registry.addCategory(ARCANE_FADING);
        registry.addWorkstation(ARCANE_FADING, EmiStack.of(AffinityItems.ARCANE_FADE_BUCKET));

        ArcaneFadeBlock.forEachGroup((id, item, items) -> {
            registry.addRecipe(new ArcaneFadingEmiRecipe(items, item, id));
        });
    }

    public static EmiIngredient veryCoolFeatureYouGotThereEmi(Ingredient ingredient) {
        if (ingredient.getCustomIngredient() instanceof PotionIngredient) {
            return new ListEmiIngredient(ingredient.getCustomIngredient().getMatchingStacks().stream().map(EmiStack::of).toList(), 1);
        }

        return EmiIngredient.of(ingredient);
    }

    public static SlotWidget slot(EmiIngredient stack, int x, int y) {
        return new AffinitySlotWidget(stack, x, y);
    }

    public static Widget arrow(int x, int y) {
        return new TextureWidget(
                EmiTexture.EMPTY_ARROW.texture, x, y,
                EmiTexture.EMPTY_ARROW.width, EmiTexture.EMPTY_ARROW.height, EmiTexture.EMPTY_ARROW.u, EmiTexture.EMPTY_ARROW.v
        );
    }
}

