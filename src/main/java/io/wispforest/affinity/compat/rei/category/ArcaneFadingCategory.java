package io.wispforest.affinity.compat.rei.category;

import dev.architectury.event.CompoundEventResult;
import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.compat.rei.display.ArcaneFadingDisplay;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.component.BlockComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.block.Block;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemConvertible;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ArcaneFadingCategory implements DisplayCategory<ArcaneFadingDisplay> {

    private static @Nullable EntryStack<?> focusedBlock = null;

    @Override
    public List<Widget> setupDisplay(ArcaneFadingDisplay display, Rectangle bounds) {
        focusedBlock = null;

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

            updater = Widgets.createDrawableWidget((context, mouseX, mouseY, delta) -> {
                if (System.currentTimeMillis() - lastUpdateTime.longValue() <= 1000) return;

                lastUpdateTime.setValue(System.currentTimeMillis());
                lastIndex.setValue((lastIndex.getValue() + 1) % display.inputs.size());

                blockContainer.<FlowLayout>configure(layout -> {
                    var input = display.inputs.get(lastIndex.getValue());

                    layout.clearChildren();
                    layout.child(makeBlockEntry(input));
                });
            });

            root.child(adapter.wrap(Widgets.createArrow(ReiUIAdapter.LAYOUT)));
            root.child(makeBlockEntry(display.output));
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

    private BlockComponent makeBlockEntry(ItemConvertible block) {
        return Components.block(((Block) block).getDefaultState()).configure(component -> {
            var tooltip = EntryStacks.of(block).getTooltip(TooltipContext.ofMouse()).entries().stream().map(entry -> {
                if (entry.isText()) {
                    return TooltipComponent.of(entry.getAsText().asOrderedText());
                } else {
                    var data = entry.getAsTooltipComponent();
                    return Objects.requireNonNullElseGet(
                            TooltipComponentCallback.EVENT.invoker().getComponent(data),
                            () -> TooltipComponent.of(data)
                    );
                }
            }).toList();

            component.sizing(Sizing.fixed(32)).tooltip(tooltip);
            component.mouseEnter().subscribe(() -> focusedBlock = EntryStacks.of(block));
            component.mouseLeave().subscribe(() -> focusedBlock = null);
            component.mouseDown().subscribe((mouseX_, mouseY_, button) -> {
                if (ConfigObject.getInstance().getRecipeKeybind().getType() != InputUtil.Type.MOUSE && button == 0) {
                    return ViewSearchBuilder.builder().addRecipesFor(EntryStacks.of(block)).open();
                } else if (ConfigObject.getInstance().getUsageKeybind().getType() != InputUtil.Type.MOUSE && button == 1) {
                    return ViewSearchBuilder.builder().addUsagesFor(EntryStacks.of(block)).open();
                } else {
                    return false;
                }
            });
        });
    }

    static {
        ScreenRegistry.getInstance().registerFocusedStack((screen, mouse) -> {
            if (!(screen instanceof DisplayScreen displayScreen) || displayScreen.getCurrentCategory().getCategoryIdentifier() != AffinityReiCommonPlugin.ARCANE_FADING) {
                return CompoundEventResult.pass();
            }

            if (focusedBlock == null) return CompoundEventResult.pass();
            return CompoundEventResult.interruptTrue(focusedBlock);
        });
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
