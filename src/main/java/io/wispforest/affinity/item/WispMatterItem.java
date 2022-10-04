package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.wisps.WispType;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WispMatterItem extends Item {

    private static final MutableText TRANSLATED_NAME = Text.translatable(Util.createTranslationKey("item", Affinity.id("wisp_matter")));
    private final WispType type;

    public WispMatterItem(WispType type) {
        super(AffinityItems.settings(AffinityItemGroup.MAIN));
        this.type = type;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var world = context.getWorld();

        if (this == AffinityItems.INERT_WISP_MATTER) {
            AffinityParticleSystems.ABERRANT_CALLING_SUCCESS.spawn(world, Vec3d.ofCenter(context.getBlockPos().up()));
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
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
        tooltip.add(TextOps.withColor(type.icon(), type.color()).append(" ").append(Text.translatable(type.translationKey()).formatted(Formatting.GRAY)));
    }

    public WispType wispType() {
        return type;
    }
}
