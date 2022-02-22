package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.wisps.WispType;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WispMatterItem extends Item {

    private static final TranslatableText TRANSLATED_NAME = new TranslatableText(Util.createTranslationKey("item", Affinity.id("wisp_matter")));
    private final WispType type;

    public WispMatterItem(WispType type) {
        super(AffinityItems.settings(0));
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(TextOps.withColor(type.icon(), type.color()).append(" ").append(new TranslatableText(type.translationKey()).formatted(Formatting.GRAY)));
    }

    public WispType wispType() {
        return type;
    }
}
