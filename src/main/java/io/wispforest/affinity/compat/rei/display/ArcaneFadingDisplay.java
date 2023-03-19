package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class ArcaneFadingDisplay implements Display {

    private final List<EntryIngredient> inputEntries;
    private final List<EntryIngredient> outputEntries;
    private final Identifier id;

    public final List<ItemConvertible> inputs;
    public final ItemConvertible output;
    public final boolean displayAsBlocks;

    public ArcaneFadingDisplay(List<ItemConvertible> inputs, ItemConvertible output, Identifier id) {
        this.inputs = inputs;
        this.output = output;
        this.id = id;

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
    public Optional<Identifier> getDisplayLocation() {
        return Optional.of(id);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ARCANE_FADING;
    }
}
