package io.wispforest.affinity.compat.rei.display;

import com.google.common.collect.ImmutableList;
import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.recipe.PotionMixingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Optional;

public class PotionMixingDisplay extends BasicDisplay {

    public final PotionMixingRecipe recipe;

    public PotionMixingDisplay(PotionMixingRecipe recipe) {
        super(Util.make(() -> {
            final var inputBuilder = new ImmutableList.Builder<EntryIngredient>();
            recipe.getItemInputs().forEach(ingredient -> inputBuilder.add(EntryIngredients.ofIngredient(ingredient.itemPredicate())));
            recipe.getEffectInputs().forEach(effect -> inputBuilder.add(EntryIngredients.of(AffinityReiCommonPlugin.EFFECT_ENTRY_TYPE, List.of(effect))));
            return inputBuilder.build();
        }), Util.make(() -> {
            final var potionStack = new ItemStack(Items.POTION);
            PotionUtil.setPotion(potionStack, recipe.potionOutput());
            return List.of(EntryIngredients.of(potionStack));
        }), Optional.of(recipe.getId()));

        this.recipe = recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.POTION_MIXING;
    }
}
