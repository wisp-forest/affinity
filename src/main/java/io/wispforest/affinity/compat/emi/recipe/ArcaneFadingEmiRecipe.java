package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import io.wispforest.affinity.compat.emi.BlockStateEmiStack;
import io.wispforest.affinity.compat.emi.EmiUIAdapter;
import io.wispforest.affinity.compat.emi.LargeSlotWidget;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;

import java.util.List;

public class ArcaneFadingEmiRecipe extends BasicEmiRecipe {

    private final EmiIngredient inputDisplay;
    private final EmiIngredient outputDisplay;
    private final boolean displayAsBlocks;

    public ArcaneFadingEmiRecipe(List<ItemConvertible> inputs, ItemConvertible output, Identifier id) {
        super(AffinityEmiPlugin.ARCANE_FADING, id, 150, 44);

        this.inputs = List.of(EmiIngredient.of(inputs.stream().map(EmiStack::of).toList()));
        this.outputs = List.of(EmiStack.of(output));
        this.displayAsBlocks = inputs.stream().allMatch(Block.class::isInstance) && output instanceof Block;

        if (this.displayAsBlocks) {
            this.inputDisplay = EmiIngredient.of(inputs.stream().map(Block.class::cast).map(block -> new BlockStateEmiStack(block.getDefaultState())).toList());
            this.outputDisplay = new BlockStateEmiStack(((Block) output).getDefaultState());
        } else {
            this.inputDisplay = this.inputs.get(0);
            this.outputDisplay = this.outputs.get(0);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var bounds = new Bounds(0, 0, widgets.getWidth(), widgets.getHeight());
        var adapter = new EmiUIAdapter<>(bounds, Containers::horizontalFlow);

        var root = adapter.rootComponent();

        root.gap(6).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);

        root.child(adapter.wrap((x, y) -> this.displayAsBlocks ? new LargeSlotWidget(this.inputDisplay, x, y) : AffinityEmiPlugin.slot(this.inputDisplay, x, y)));
        root.child(adapter.wrap(AffinityEmiPlugin::arrow));
        root.child(adapter.wrap((x, y) -> this.displayAsBlocks ? new LargeSlotWidget(this.outputDisplay, x, y).large(true).recipeContext(this) : AffinityEmiPlugin.slot(this.outputDisplay, x, y).large(true).recipeContext(this)));

        adapter.prepare();
        widgets.add(adapter);
    }
}
