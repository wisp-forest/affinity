package io.wispforest.affinity.compat.rei.category;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.compat.rei.display.OrnamentCarvingDisplay;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.VerticalAlignment;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.text.Text;

import java.util.List;

public class OrnamentCarvingCategory implements DisplayCategory<OrnamentCarvingDisplay> {

    @Override
    public List<Widget> setupDisplay(OrnamentCarvingDisplay display, Rectangle bounds) {
        var adapter = new ReiUIAdapter<>(bounds, Containers::horizontalFlow);
        var root = adapter.rootComponent();

        root.gap(5).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        root.child(adapter.wrap(Widgets.createRecipeBase(bounds)).positioning(Positioning.absolute(0, 0)));

        root.child(adapter.wrap(Widgets::createSlot, slot -> slot.entries(display.getInputEntries().get(0))));
        root.child(adapter.wrap(Widgets.createArrow(ReiUIAdapter.LAYOUT)));
        root.child(adapter.wrap(Widgets::createSlot, slot -> slot.entries(display.getOutputEntries().get(0))));

        adapter.prepare();
        return List.of(adapter);
    }

    @Override
    public int getDisplayHeight() {
        return DisplayCategory.super.getDisplayHeight() - 30;
    }

    @Override
    public int getDisplayWidth(OrnamentCarvingDisplay display) {
        return DisplayCategory.super.getDisplayWidth(display) - 50;
    }

    @Override
    public CategoryIdentifier<? extends OrnamentCarvingDisplay> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ORNAMENT_CARVING;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("category.affinity.ornament_carving");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AffinityBlocks.RITUAL_SOCLE_COMPOSER);
    }
}
