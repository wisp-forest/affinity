package io.wispforest.affinity.compat.rei.category;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.compat.rei.display.ArcaneFadingDisplay;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.List;

public class ArcaneFadingCategory implements DisplayCategory<ArcaneFadingDisplay> {

    @Override
    public List<Widget> setupDisplay(ArcaneFadingDisplay display, Rectangle bounds) {
        var adapter = new ReiUIAdapter<>(bounds, Containers::horizontalFlow);
        var root = adapter.rootComponent();

        Widget updater = null;

        root.gap(6).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        root.child(adapter.wrap(Widgets.createRecipeBase(bounds)).positioning(Positioning.absolute(0, 0)));

        if (display.displayAsBlocks) {
            var blockContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
            root.child(blockContainer);

            var lastUpdateTime = new MutableLong(0);
            var lastIndex = new MutableInt(-1);

            updater = Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                if (System.currentTimeMillis() - lastUpdateTime.longValue() <= 1000) return;

                lastUpdateTime.setValue(System.currentTimeMillis());
                lastIndex.setValue((lastIndex.getValue() + 1) % display.inputs.size());

                blockContainer.<FlowLayout>configure(layout -> {
                    layout.clearChildren();
                    layout.child(Components.block(((Block) display.inputs.get(lastIndex.getValue())).getDefaultState()).sizing(Sizing.fixed(32)));
                });
            });

            root.child(adapter.wrap(Widgets.createArrow(ReiUIAdapter.LAYOUT)));
            root.child(Components.block(((Block) display.output).getDefaultState()).sizing(Sizing.fixed(32)));
        } else {
            root.child(adapter.wrap(Widgets::createSlot, slot -> slot.entries(display.getInputEntries().get(0)).markInput()));

            root.child(adapter.wrap(Widgets.createArrow(ReiUIAdapter.LAYOUT)));

            root.child(Containers.verticalFlow(Sizing.content(), Sizing.content())
                    .child(adapter.wrap((WidgetWithBounds) Widgets.createResultSlotBackground(ReiUIAdapter.LAYOUT)))
                    .child(adapter.wrap(Widgets::createSlot, slot -> slot.entries(display.getOutputEntries().get(0)).markOutput().disableBackground()).positioning(Positioning.relative(50, 50)))
            );
        }

        adapter.prepare();
        return updater == null
                ? List.of(adapter)
                : List.of(adapter, updater);
    }

    @Override
    public int getDisplayHeight() {
        return 44;
    }

    @Override
    public CategoryIdentifier<? extends ArcaneFadingDisplay> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ARCANE_FADING;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("category.affinity.arcane_fading");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AffinityItems.ARCANE_FADE_BUCKET);
    }
}
