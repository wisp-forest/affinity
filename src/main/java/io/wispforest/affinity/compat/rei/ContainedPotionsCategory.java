package io.wispforest.affinity.compat.rei;

import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Arrow;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;

import java.util.List;

public class ContainedPotionsCategory implements DisplayCategory<ContainedPotionsDisplay> {
    @Override
    public List<Widget> setupDisplay(ContainedPotionsDisplay display, Rectangle bounds) {
        var adapter = new ReiUIAdapter<>(bounds, Containers::horizontalFlow);
        var root = adapter.rootComponent();
        root.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);
        root.allowOverflow(true);

        root.child(adapter.wrap(Widgets.createRecipeBase(bounds)).positioning(Positioning.absolute(0, 0)));

        root.child(adapter.wrap(
                Widgets::createSlot,
                slot -> slot.entries(display.getOutputEntries().get(0)).markInput().disableHighlight().disableBackground()
            )
        );

        root.child(adapter.wrap(
            Widgets::createArrow,
            Arrow::disableAnimation
        ).margins(Insets.horizontal(5)));

        var potionContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        root.child(potionContainer);

        for (var input : display.getInputEntries()) {
            potionContainer.child(adapter.wrap(
                Widgets::createSlot,
                slot -> slot.entries(input).markOutput()
            ).margins(Insets.of(1)));
        }

        adapter.prepare();
        return List.of(adapter);
    }

    @Override
    public int getDisplayHeight() {
        return 105;
    }

    @Override
    public int getDisplayWidth(ContainedPotionsDisplay display) {
        return 165;
    }

    @Override
    public CategoryIdentifier<? extends ContainedPotionsDisplay> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.CONTAINED_POTIONS;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("category.affinity.contained_potions");
    }

    @Override
    public Renderer getIcon() {
        ItemStack stack = new ItemStack(Items.POTION);
        PotionUtil.setPotion(stack, Potions.STRENGTH);
        return EntryStacks.of(stack);
    }
}
