package io.wispforest.affinity.compat.rei;

import com.google.common.collect.ImmutableList;
import io.wispforest.affinity.misc.recipe.PotionMixingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;

import java.util.Collections;
import java.util.List;

public class PotionMixingDisplay implements Display {

    private final List<EntryIngredient> inputs;
    private final EntryIngredient output;
    private final List<StatusEffect> effects;

    public PotionMixingDisplay(PotionMixingRecipe recipe) {

        final var inputBuilder = new ImmutableList.Builder<EntryIngredient>();
        recipe.getItemInputs().forEach(ingredient -> inputBuilder.add(EntryIngredients.ofIngredient(ingredient.itemPredicate())));
        inputs = inputBuilder.build();

        final var potionStack = new ItemStack(Items.POTION);
        PotionUtil.setPotion(potionStack, recipe.potionOutput());
        output = EntryIngredients.of(potionStack);

        effects = recipe.getEffectInputs();
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.POTION_MIXING_ID;
    }

    public List<StatusEffect> getEffects() {
        return effects;
    }
}
