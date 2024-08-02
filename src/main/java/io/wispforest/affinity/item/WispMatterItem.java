package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.wisps.WispType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.List;

public class WispMatterItem extends Item {

    private static final Text TRANSLATED_NAME = Text.translatable(Util.createTranslationKey("item", Affinity.id("wisp_matter")));
    private final WispType type;

    public WispMatterItem(WispType type) {
        super(AffinityItems.settings());
        this.type = type;
    }

    @Override
    public Text getName() {
        return TRANSLATED_NAME;
    }

    @Override
    public Text getName(ItemStack stack) {
        return TRANSLATED_NAME;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(this.type.createTooltip());
    }

    public WispType wispType() {
        return type;
    }
}
