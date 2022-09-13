package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.util.BlockFinder;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.wisps.WispType;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.tag.BlockTags;
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

        if (!world.getBlockState(context.getBlockPos()).isIn(BlockTags.LOGS)) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS;

        final var results = BlockFinder.findCapped(world, context.getBlockPos(), (blockPos, state) -> {
            if (state.isIn(BlockTags.LEAVES)) {
                return !state.get(LeavesBlock.PERSISTENT);
            }

            return state.isIn(BlockTags.LOGS);
        }, 128);
        final var counted = results.byCount();

        final var player = context.getPlayer();
        final int logCount = counted.getOrDefault(AffinityBlocks.AZALEA_LOG, 0);
        final int leavesCount = counted.getOrDefault(Blocks.AZALEA_LEAVES, 0) + counted.getOrDefault(Blocks.FLOWERING_AZALEA_LEAVES, 0);

        if (logCount > 5 && leavesCount > 40) {
            player.sendMessage(Text.literal("yep, that is in fact a tree").formatted(Formatting.GREEN), false);
        } else {
            player.sendMessage(Text.literal("nope, not a tree. no.").formatted(Formatting.RED), false);
        }

        if (this == AffinityItems.VICIOUS_WISP_MATTER) {
            for (var pos : results) {
                world.breakBlock(pos, true);
            }
        }

        return ActionResult.SUCCESS;
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
