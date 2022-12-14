package io.wispforest.affinity.compat.rei.category;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.compat.rei.display.AspenInfusionDisplay;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

import java.util.List;

public class AspenInfusionCategory implements DisplayCategory<AspenInfusionDisplay> {

    public static final TagKey<Item> RECIPE_RITUAL_SOCLE_PREVIEW = TagKey.of(RegistryKeys.ITEM, Affinity.id("recipe_ritual_socle_preview"));

    @Override
    public List<Widget> setupDisplay(AspenInfusionDisplay display, Rectangle bounds) {
        var adapter = new ReiUIAdapter<>(bounds, Containers::horizontalFlow);
        var root = adapter.rootComponent();
        root.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);

        root.child(adapter.wrap(Widgets.createRecipeBase(bounds)).positioning(Positioning.absolute(0, 0)));

        // Input circle

        var inputContainer = Containers.verticalFlow(Sizing.fixed(bounds.height - 8), Sizing.fixed(bounds.height - 8));
        inputContainer.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER).margins(Insets.right(3));
        root.child(inputContainer);

        var center = new Point((bounds.height - 8) / 2 - 9, (bounds.height - 8) / 2 - 9);
        double angleStep = Math.PI / (display.getInputEntries().size() - 1) * 2;

        for (int i = 1; i < display.getInputEntries().size(); i++) {
            int index = i;
            double angle = angleStep * index - Math.PI / 2;

            inputContainer.child(adapter.wrap(Widgets::createSlot, slot -> {
                slot.markInput().entries(display.getInputEntries().get(index)).disableBackground();
            }).positioning(Positioning.absolute((int) (center.x + Math.cos(angle) * 30), (int) (center.y + Math.sin(angle) * 30))));
        }

        inputContainer.child(adapter.wrap(Widgets::createSlot, slot -> {
            slot.entries(display.getInputEntries().get(0)).disableBackground();
        }));

        // Arrow

        root.child(adapter.wrap(Widgets::createArrow, arrow -> {
            arrow.animationDurationTicks(display.recipe.getDuration());
        }).tooltip(Text.of(MathUtil.rounded(display.recipe.getDuration() / 20d, 1) + "s")));

        // Output and socle requirements

        var outputContainer = Containers.verticalFlow(Sizing.content(), Sizing.fill(100));
        outputContainer.verticalAlignment(VerticalAlignment.CENTER).margins(Insets.left(5));
        root.child(outputContainer);

        var resultSlot = Containers.verticalFlow(Sizing.content(), Sizing.content());
        resultSlot.child(adapter.wrap((WidgetWithBounds) Widgets.createResultSlotBackground(ReiUIAdapter.LAYOUT)));
        outputContainer.child(resultSlot);

        resultSlot.child(adapter.wrap(Widgets::createSlot, slot -> {
            slot.markOutput().entries(display.getOutputEntries().get(0)).disableBackground();
        }).positioning(Positioning.relative(50, 50)));

        var soclePreviewFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        soclePreviewFlow.margins(Insets.bottom(8)).positioning(Positioning.relative(50, 100));
        soclePreviewFlow.gap(3).verticalAlignment(VerticalAlignment.CENTER);
        outputContainer.child(soclePreviewFlow);

        soclePreviewFlow.child(adapter.wrap(Widgets::createSlot, slot -> {
            slot.entries(EntryIngredients.ofItemTag(RECIPE_RITUAL_SOCLE_PREVIEW).stream().map(entryStack -> {
                var stack = entryStack.<ItemStack>castValue().copy();
                stack.setCount(display.getInputEntries().size() - 1);
                return EntryStack.of(VanillaEntryTypes.ITEM, stack);
            }).toList());
        }));

        adapter.prepare();
        return List.of(adapter);
    }

    @Override
    public int getDisplayHeight() {
        return DisplayCategory.super.getDisplayHeight() + 20;
    }

    @Override
    public int getDisplayWidth(AspenInfusionDisplay display) {
        return DisplayCategory.super.getDisplayWidth(display);
    }

    @Override
    public CategoryIdentifier<? extends AspenInfusionDisplay> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ASPEN_INFUSION;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("category.affinity.aspen_infusion");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AffinityBlocks.ASP_RITE_CORE);
    }
}
