package io.wispforest.affinity.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

import java.util.List;

public class ContainedPotionsDisplay implements Display {
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public ContainedPotionsDisplay(StatusEffect effect, List<Potion> potions) {
        inputs = potions
            .stream()
            .map(x -> {
                var stack = new ItemStack(Items.POTION);
                PotionUtil.setPotion(stack, x);
                return stack;
            })
            .map(EntryIngredients::of)
            .toList();

        outputs = List.of(EntryIngredients.of(AffinityReiCommonPlugin.EFFECT_ENTRY_TYPE, List.of(effect)));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.CONTAINED_POTIONS;
    }
}
