package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.FillingArrowWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import io.wispforest.affinity.compat.emi.EmiUIAdapter;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.recipe.AspenInfusionRecipe;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import me.shedaniel.math.Point;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class AspenInfusionEmiRecipe implements EmiRecipe {

    public static final TagKey<Item> RECIPE_RITUAL_SOCLE_PREVIEW = TagKey.of(RegistryKeys.ITEM, Affinity.id("ritual_socles"));

    private final Identifier id;
    private final AspenInfusionRecipe recipe;

    public AspenInfusionEmiRecipe(Identifier id, AspenInfusionRecipe recipe) {
        this.id = id;
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return AffinityEmiPlugin.ASPEN_INFUSION;
    }

    @Override
    public @Nullable Identifier getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return Stream.concat(Stream.of(this.recipe.primaryInput), this.recipe.getIngredients().stream()).map(AffinityEmiPlugin::veryCoolFeatureYouGotThereEmi).toList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(this.recipe.getResult(null)));
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 86;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var bounds = new Bounds(0, 0, widgets.getWidth(), widgets.getHeight());
        var adapter = new EmiUIAdapter<>(bounds, Containers::horizontalFlow);

        var root = adapter.rootComponent();
        root.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);

        // Input circle

        var inputContainer = Containers.verticalFlow(Sizing.fixed(bounds.height() - 8), Sizing.fixed(bounds.height() - 8));
        inputContainer.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER).margins(Insets.right(3));
        root.child(inputContainer);

        var center = new Point((bounds.height() - 8) / 2 - 9, (bounds.height() - 8) / 2 - 9);
        double angleStep = Math.PI / (this.getInputs().size() - 1) * 2;

        for (int i = 1; i < this.getInputs().size(); i++) {
            int index = i;
            double angle = angleStep * (index - 1) - Math.PI / 2;

            inputContainer.child(adapter.wrap((x, y) -> {
                return new SlotWidget(this.getInputs().get(index), x, y).drawBack(false);
            }).positioning(Positioning.absolute((int) (center.x + Math.cos(angle) * 30), (int) (center.y + Math.sin(angle) * 30))));
        }

        inputContainer.child(adapter.wrap((x, y) -> new SlotWidget(this.getInputs().get(0), x, y)));

        // Arrow

        root.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).<FlowLayout>configure(container -> {
            container.gap(5).horizontalAlignment(HorizontalAlignment.CENTER);

            container.child(adapter.wrap((x, y) -> new FillingArrowWidget(x, y, this.recipe.duration * 50))
                    .tooltip(Text.of(MathUtil.rounded(this.recipe.duration / 20d, 1) + "s")));

            if (this.recipe.fluxCostPerTick != 0) {
                container.child(Components.label(Text.of(this.recipe.fluxCostPerTick * this.recipe.duration + "\n" + "flux"))
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .color(Color.ofRgb(0x3f3f3f))
                ).padding(Insets.top(25));
            }
        }));

        // Output and socle requirements

        var outputContainer = Containers.verticalFlow(Sizing.content(), Sizing.fill(100));
        outputContainer.verticalAlignment(VerticalAlignment.CENTER).margins(Insets.left(5));
        root.child(outputContainer);

        outputContainer.child(adapter.wrap((x, y) -> new SlotWidget(EmiStack.of(this.recipe.getResult(null)), x, y).large(true)));

        var soclePreviewFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        soclePreviewFlow.margins(Insets.bottom(8)).positioning(Positioning.relative(50, 100));
        soclePreviewFlow.gap(3).verticalAlignment(VerticalAlignment.CENTER);
        outputContainer.child(soclePreviewFlow);

        soclePreviewFlow.child(adapter.wrap((x, y) -> {
            return new SlotWidget(EmiIngredient.of(RECIPE_RITUAL_SOCLE_PREVIEW, this.getInputs().size() - 1), x, y);
        }));

        adapter.prepare();
        widgets.add(adapter);

    }
}
