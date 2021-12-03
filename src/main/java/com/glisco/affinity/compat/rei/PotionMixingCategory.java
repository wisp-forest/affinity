package com.glisco.affinity.compat.rei;

import com.glisco.affinity.Affinity;
import com.glisco.affinity.registries.AffinityBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

public class PotionMixingCategory implements DisplayCategory<PotionMixingDisplay> {

    public static final CategoryIdentifier<PotionMixingDisplay> ID = CategoryIdentifier.of(Affinity.id("potion_mixing"));

    @Override
    public List<Widget> setupDisplay(PotionMixingDisplay display, Rectangle bounds) {
        Point origin = bounds.getLocation();

        final var widgets = new ArrayList<Widget>();

        for (int i = 0; i < display.getInputEntries().size(); i++) {
            widgets.add(Widgets.createSlot(new Point(origin.x + 5, origin.y - 25 + 20 * i)).entries(display.getInputEntries().get(i)));
        }

        widgets.add(Widgets.createSlot(new Point(origin.x + 110, origin.y + 32)).entries(display.getOutputEntries().get(0)).disableBackground());
        widgets.add(Widgets.createResultSlotBackground(new Point(origin.x + 110, origin.y + 32)));

        widgets.add(Widgets.createArrow(new Point(origin.x + 59, origin.y + 30)).animationDurationTicks(100));

        for (int i = 0; i < display.getEffects().size(); i++) {
            final var effect = display.getEffects().get(i);
            final var text = new TranslatableText(effect.getTranslationKey());
            widgets.add(Widgets.createLabel(new Point(origin.x + 70, origin.y - 20 + 10 * i), text).noShadow().color(0x3F3F3F));
        }

        return widgets;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AffinityBlocks.BREWING_CAULDRON);
    }

    @Override
    public Text getTitle() {
        return Text.of("potion mixin™");
    }

    @Override
    public CategoryIdentifier<? extends PotionMixingDisplay> getCategoryIdentifier() {
        return ID;
    }
}
