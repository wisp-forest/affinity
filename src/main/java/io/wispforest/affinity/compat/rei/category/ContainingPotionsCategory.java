package io.wispforest.affinity.compat.rei.category;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.compat.rei.display.ContainingPotionsDisplay;
import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
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
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class ContainingPotionsCategory implements DisplayCategory<ContainingPotionsDisplay> {
    @Override
    public List<Widget> setupDisplay(ContainingPotionsDisplay display, Rectangle bounds) {
        var adapter = new ReiUIAdapter<>(bounds, Containers::verticalFlow);
        var root = adapter.rootComponent();
        root.horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        root.allowOverflow(true);

        root.child(adapter.wrap(Widgets.createRecipeBase(bounds)).positioning(Positioning.absolute(0, 0)));

        var contentPane = Containers.verticalFlow(Sizing.content(), Sizing.content());
        contentPane.horizontalAlignment(HorizontalAlignment.CENTER);
        contentPane.margins(Insets.right(4));

        contentPane.child(adapter.wrap(
                        Widgets::createSlot,
                        slot -> slot.entries(display.getOutputEntries().get(0)).markInput().disableHighlight().disableBackground()
                ).margins(Insets.bottom(10))
        );

        var inputs = display.getInputEntries();
        inputs:
        for (int row = 0; row < MathHelper.ceilDiv(inputs.size(), 6); row++) {
            var rowContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            contentPane.child(rowContainer);

            for (int column = 0; column < 6; column++) {
                int idx = row * 6 + column;
                if (idx >= inputs.size()) break inputs;

                rowContainer.child(adapter.wrap(
                        Widgets::createSlot,
                        slot -> slot.entries(inputs.get(idx)).markOutput()
                ).margins(Insets.of(1)));
            }
        }

        root.child(Containers.verticalScroll(Sizing.content(), Sizing.fixed(bounds.height - 8), contentPane)
                .padding(Insets.top(4))
                .margins(Insets.left(4))
        );
        adapter.prepare();
        return List.of(adapter);
    }

    @Override
    public CategoryIdentifier<? extends ContainingPotionsDisplay> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.CONTAINING_POTIONS;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("category.affinity.containing_potions");
    }

    @Override
    public Renderer getIcon() {
        ItemStack stack = new ItemStack(Items.POTION);
        PotionUtil.setPotion(stack, Potions.STRENGTH);
        return EntryStacks.of(stack);
    }
}
