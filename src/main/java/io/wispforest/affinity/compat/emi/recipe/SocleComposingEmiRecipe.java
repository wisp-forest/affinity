package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import io.wispforest.affinity.compat.emi.EmiUIAdapter;
import io.wispforest.affinity.compat.rei.display.SocleComposingDisplay;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SocleComposingEmiRecipe implements EmiRecipe {

    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public SocleComposingEmiRecipe(RitualSocleType type, SocleComposingDisplay.Action action) {
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
    public EmiRecipeCategory getCategory() {
        return AffinityEmiPlugin.SOCLE_COMPOSING;
    }

    @Override
    public @Nullable Identifier getId() {
        return null;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return this.outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 20;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var bounds = new Bounds(0, 0, widgets.getWidth(), widgets.getHeight());
        var adapter = new EmiUIAdapter<>(bounds, Containers::horizontalFlow);

        var root = adapter.rootComponent();
        root.gap(5).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);

        for (var input : this.inputs) {
            root.child(adapter.wrap((x, y) -> new SlotWidget(input, x, y)));
        }

        root.child(adapter.wrap((x, y) -> new TextureWidget(
                EmiTexture.EMPTY_ARROW.texture, x, y,
                EmiTexture.EMPTY_ARROW.width, EmiTexture.EMPTY_ARROW.height, EmiTexture.EMPTY_ARROW.u, EmiTexture.EMPTY_ARROW.v
        )));

        for (var output : this.outputs) {
            root.child(adapter.wrap((x, y) -> new SlotWidget(output, x, y)));
        }

        adapter.prepare();
        widgets.add(adapter);

    }
}
