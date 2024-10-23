package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.FillingArrowWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import io.wispforest.affinity.compat.emi.EmiUIAdapter;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.recipe.SpiritAssimilationRecipe;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

public class SpiritAssimilationEmiRecipe extends BasicEmiRecipe {

    private final SpiritAssimilationRecipe recipe;

    public SpiritAssimilationEmiRecipe(Identifier id, SpiritAssimilationRecipe recipe) {
        super(AffinityEmiPlugin.SPIRIT_ASSIMILATION, id, 180, 106);
        this.recipe = recipe;

        this.inputs = Stream.concat(
            this.recipe.coreInputs.stream(),
            this.recipe.getIngredients().stream()
        ).map(AffinityEmiPlugin::veryCoolFeatureYouGotThereEmi).toList();
        this.outputs = List.of(EmiStack.of(this.recipe.getResult(null)));
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

        int centerX = (bounds.height() - 8) / 2 - 9, centerY = (bounds.height() - 8) / 2 - 9;
        double angleStep = Math.PI / (this.getInputs().size() - 4) * 2;

        for (int i = 4; i < this.getInputs().size(); i++) {
            int index = i;
            double angle = angleStep * (index - 4) - Math.PI / 2;

            inputContainer.child(adapter.wrap((x, y) -> {
                return AffinityEmiPlugin.slot(this.getInputs().get(index), x, y).drawBack(false);
            }).positioning(Positioning.absolute((int) (centerX + Math.cos(angle) * 40), (int) (centerY + Math.sin(angle) * 40))));
        }

        var inputGrid = Containers.grid(Sizing.content(), Sizing.content(), 2, 2);
        for (int i = 0; i < 4; i++) {
            final int index = i;
            inputGrid.child(adapter.wrap((x, y) -> {
                return AffinityEmiPlugin.slot(this.getInputs().get(index), x, y);
            }), i / 2, i % 2);
        }
        inputContainer.child(inputGrid);

        // Arrow and sacrifice

        root.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).<FlowLayout>configure(container -> {
            container.gap(5).horizontalAlignment(HorizontalAlignment.CENTER);

            container.child((this.recipe.entityType == EntityType.PLAYER
                    ? Components.entity(Sizing.fixed(30), EntityComponent.createRenderablePlayer(MinecraftClient.getInstance().player.getGameProfile()))
                    : Components.entity(Sizing.fixed(30), this.recipe.entityType, this.recipe.entityNbt()))
                    .scaleToFit(true).tooltip(this.recipe.entityType.getName()))
                .child(adapter.wrap((x, y) -> new FillingArrowWidget(x, y, this.recipe.duration * 50))
                    .tooltip(Text.of(MathUtil.rounded(this.recipe.duration / 20d, 1) + "s")));

            if (this.recipe.fluxCostPerTick != 0) {
                container.child(Components.label(Text.of(this.recipe.fluxCostPerTick * this.recipe.duration + "\n" + "flux"))
                    .horizontalTextAlignment(HorizontalAlignment.CENTER)
                    .color(Color.ofRgb(0x3f3f3f))
                    .margins(Insets.top(10)));
            } else {
                container.padding(Insets.bottom(35));
            }
        }));

        // Output and socle requirements

        var outputContainer = Containers.verticalFlow(Sizing.content(), Sizing.fill(100));
        outputContainer.verticalAlignment(VerticalAlignment.CENTER).margins(Insets.left(5));
        root.child(outputContainer);

        outputContainer.child(adapter.wrap((x, y) -> AffinityEmiPlugin.slot(this.getOutputs().get(0), x, y).large(true).recipeContext(this)));

        var soclePreviewFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        soclePreviewFlow.margins(Insets.bottom(8)).positioning(Positioning.relative(50, 100));
        soclePreviewFlow.gap(3).verticalAlignment(VerticalAlignment.CENTER);
        outputContainer.child(soclePreviewFlow);

        soclePreviewFlow.child(adapter.wrap((x, y) -> {
            return AffinityEmiPlugin.slot(EmiIngredient.of(AspenInfusionEmiRecipe.RECIPE_RITUAL_SOCLE_PREVIEW, this.getInputs().size() - 1), x, y);
        }));

        adapter.prepare();
        widgets.add(adapter);
    }
}
