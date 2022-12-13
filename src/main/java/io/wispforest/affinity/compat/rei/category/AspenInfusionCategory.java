package io.wispforest.affinity.compat.rei.category;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.compat.rei.display.AspenInfusionDisplay;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

import java.util.List;

public class AspenInfusionCategory implements DisplayCategory<AspenInfusionDisplay> {

    public static final TagKey<Item> RECIPE_RITUAL_SOCLE_PREVIEW = TagKey.of(RegistryKeys.ITEM, Affinity.id("recipe_ritual_socle_preview"));

    @Override
    public List<Widget> setupDisplay(AspenInfusionDisplay display, Rectangle bounds) {
        var adapter = new ReiUIAdapter<>(bounds, Containers::verticalFlow);
        var root = adapter.rootComponent();
        root.gap(10).verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);

        root.child(adapter.wrap(Widgets.createRecipeBase(bounds)).positioning(Positioning.absolute(0, 0)));

        var inputFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        root.child(inputFlow);
        for (var input : display.getInputEntries()) {
            inputFlow.child(adapter.wrap(Widgets::createSlot, slot -> {
                slot.markInput().entries(input);
            }));
        }

        root.child(adapter.wrap(Widgets::createSlot, slot -> {
            slot.markOutput().entries(display.getOutputEntries().get(0));
        }));

        var soclePreviewFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        soclePreviewFlow.positioning(Positioning.relative(0, 100)).margins(Insets.of(0, 8, 8, 0));
        soclePreviewFlow.gap(3).verticalAlignment(VerticalAlignment.CENTER);
        root.child(soclePreviewFlow);

        soclePreviewFlow.child(adapter.wrap(Widgets::createSlot, slot -> {
            slot.entries(EntryIngredients.ofItemTag(RECIPE_RITUAL_SOCLE_PREVIEW));
        })).child(Components.label(Text.literal("x" + display.getInputEntries().size())).shadow(true));

        adapter.prepare();
        return List.of(adapter);
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
