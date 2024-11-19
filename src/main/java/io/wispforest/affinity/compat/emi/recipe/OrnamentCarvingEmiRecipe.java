package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import io.wispforest.affinity.compat.emi.EmiUIAdapter;
import io.wispforest.affinity.recipe.OrnamentCarvingRecipe;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.util.Identifier;

import java.util.List;

public class OrnamentCarvingEmiRecipe extends BasicEmiRecipe {

    public OrnamentCarvingEmiRecipe(Identifier id, OrnamentCarvingRecipe recipe) {
        super(AffinityEmiPlugin.ORNAMENT_CARVING, id, 120, 20);
        this.id = id;

        this.inputs = List.of(AffinityEmiPlugin.veryCoolFeatureYouGotThereEmi(recipe.input));
        this.outputs = List.of(EmiStack.of(recipe.getResult(null)));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var bounds = new Bounds(0, 0, widgets.getWidth(), widgets.getHeight());
        var adapter = new EmiUIAdapter<>(bounds, Containers::horizontalFlow);

        var root = adapter.rootComponent();
        root.gap(5).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);

        root.child(adapter.wrap((x, y) -> AffinityEmiPlugin.slot(this.getInputs().get(0), x, y)));
        root.child(adapter.wrap(AffinityEmiPlugin::arrow));
        root.child(adapter.wrap((x, y) -> AffinityEmiPlugin.slot(this.getOutputs().get(0), x, y).recipeContext(this)));

        adapter.prepare();
        widgets.add(adapter);
    }
}
