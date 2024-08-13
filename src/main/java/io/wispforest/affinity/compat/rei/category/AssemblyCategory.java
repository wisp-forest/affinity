package io.wispforest.affinity.compat.rei.category;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.object.AffinityBlocks;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.client.categories.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AssemblyCategory extends DefaultCraftingCategory {

    @Override
    public List<Widget> setupDisplay(DefaultCraftingDisplay<?> display, Rectangle bounds) {
        var widgets = new ArrayList<>(super.setupDisplay(display, bounds));
        widgets.add(Widgets.createTexturedWidget(Affinity.id("textures/gui/assembly_augment.png"), new Rectangle(bounds.getMaxX() - 13, bounds.y + (bounds.height - 34) / 2, 6, 34), 176, 83));
        return widgets;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("category.affinity.assembly");
    }

    @Override
    public CategoryIdentifier<? extends DefaultCraftingDisplay<?>> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.ASSEMBLY;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AffinityBlocks.ASSEMBLY_AUGMENT);
    }
}
