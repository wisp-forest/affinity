package io.wispforest.affinity.compat.rei.category;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.compat.rei.display.SocleComposingDisplay;
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

public class SocleComposingCategory implements DisplayCategory<SocleComposingDisplay> {

    @Override
    public List<Widget> setupDisplay(SocleComposingDisplay display, Rectangle bounds) {
        var adapter = new ReiUIAdapter<>(bounds, Containers::horizontalFlow);
        var root = adapter.rootComponent();

        root.gap(5).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        root.child(adapter.wrap(Widgets.createRecipeBase(bounds)).positioning(Positioning.absolute(0, 0)));

        for (var input : display.getInputEntries()) {
            root.child(adapter.wrap(Widgets::createSlot, slot -> slot.entries(input)));
        }

        root.child(adapter.wrap(Widgets.createArrow(ReiUIAdapter.LAYOUT)));

        for (var output : display.getOutputEntries()) {
            root.child(adapter.wrap(Widgets::createSlot, slot -> slot.entries(output)));
        }

        adapter.prepare();
        return List.of(adapter);
    }

    @Override
    public int getDisplayHeight() {
        return DisplayCategory.super.getDisplayHeight() - 30;
    }

    @Override
    public int getDisplayWidth(SocleComposingDisplay display) {
        return DisplayCategory.super.getDisplayWidth(display) - 20;
    }

    @Override
    public CategoryIdentifier<? extends SocleComposingDisplay> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.SOCLE_COMPOSING;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("category.affinity.socle_composing");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AffinityBlocks.RITUAL_SOCLE_COMPOSER);
    }
}
