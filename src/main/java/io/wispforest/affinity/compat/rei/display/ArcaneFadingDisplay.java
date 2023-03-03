package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;

import java.util.List;

public class ArcaneFadingDisplay implements Display {

    private final List<EntryIngredient> inputEntries;
    private final List<EntryIngredient> outputEntries;

    public final List<ItemConvertible> inputs;
    public final ItemConvertible output;
    public final boolean displayAsBlocks;

    public ArcaneFadingDisplay(List<ItemConvertible> inputs, ItemConvertible output) {
        this.inputs = inputs;
        this.output = output;

        this.displayAsBlocks = inputs.stream().allMatch(Block.class::isInstance) && output instanceof Block;

        this.inputEntries = List.of(EntryIngredients.ofItems(inputs));
        this.outputEntries = List.of(EntryIngredients.of(output));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return this.inputEntries;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return this.outputEntries;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ARCANE_FADING;
    }
}
