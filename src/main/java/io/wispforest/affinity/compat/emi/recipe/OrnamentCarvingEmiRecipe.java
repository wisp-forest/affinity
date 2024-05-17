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
import io.wispforest.affinity.recipe.OrnamentCarvingRecipe;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OrnamentCarvingEmiRecipe implements EmiRecipe {

    private final Identifier id;
    private final OrnamentCarvingRecipe recipe;

    public OrnamentCarvingEmiRecipe(Identifier id, OrnamentCarvingRecipe recipe) {
        this.id = id;
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return AffinityEmiPlugin.ORNAMENT_CARVING;
    }

    @Override
    public @Nullable Identifier getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(AffinityEmiPlugin.veryCoolFeatureYouGotThereEmi(this.recipe.input));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(this.recipe.getResult(null)));
    }

    @Override
    public int getDisplayWidth() {
        return 120;
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

        root.child(adapter.wrap((x, y) -> new SlotWidget(this.getInputs().get(0), x, y)));
        root.child(adapter.wrap((x, y) -> new TextureWidget(
                EmiTexture.EMPTY_ARROW.texture, x, y,
                EmiTexture.EMPTY_ARROW.width, EmiTexture.EMPTY_ARROW.height, EmiTexture.EMPTY_ARROW.u, EmiTexture.EMPTY_ARROW.v
        )));
        root.child(adapter.wrap((x, y) -> new SlotWidget(this.getOutputs().get(0), x, y)));

        adapter.prepare();
        widgets.add(adapter);
    }
}
