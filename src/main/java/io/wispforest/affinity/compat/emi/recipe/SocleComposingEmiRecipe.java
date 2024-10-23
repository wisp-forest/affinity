package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import io.wispforest.affinity.compat.emi.EmiUIAdapter;
import io.wispforest.affinity.compat.rei.display.SocleComposingDisplay;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.VerticalAlignment;

import java.util.List;

public class SocleComposingEmiRecipe extends BasicEmiRecipe {

    public SocleComposingEmiRecipe(RitualSocleType type, SocleComposingDisplay.Action action) {
        super(AffinityEmiPlugin.SOCLE_COMPOSING, null, 130, 20);
        this.inputs = switch (action) {
            case CRAFT -> List.of(EmiStack.of(AffinityBlocks.BLANK_RITUAL_SOCLE), EmiStack.of(type.ornamentItem()));
            case UNCRAFT -> List.of(EmiStack.of(type.socleBlock()));
        };

        this.outputs = switch (action) {
            case CRAFT -> List.of(EmiStack.of(type.socleBlock()));
            case UNCRAFT -> List.of(EmiStack.of(AffinityBlocks.BLANK_RITUAL_SOCLE), EmiStack.of(type.ornamentItem()));
        };
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var bounds = new Bounds(0, 0, widgets.getWidth(), widgets.getHeight());
        var adapter = new EmiUIAdapter<>(bounds, Containers::horizontalFlow);

        var root = adapter.rootComponent();
        root.gap(5).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);

        for (var input : this.inputs) {
            root.child(adapter.wrap((x, y) -> AffinityEmiPlugin.slot(input, x, y)));
        }

        root.child(adapter.wrap(AffinityEmiPlugin::arrow));

        for (var output : this.outputs) {
            root.child(adapter.wrap((x, y) -> AffinityEmiPlugin.slot(output, x, y).recipeContext(this)));
        }

        adapter.prepare();
        widgets.add(adapter);

    }
}
