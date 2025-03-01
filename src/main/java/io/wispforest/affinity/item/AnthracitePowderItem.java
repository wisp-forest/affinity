package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public class AnthracitePowderItem extends Item implements DirectInteractionHandler {

    public AnthracitePowderItem() {
        super(AffinityItems.settings());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var blockEntity = context.getWorld().getBlockEntity(context.getBlockPos());
        if (!(blockEntity instanceof AssemblyAugmentBlockEntity augment)) return ActionResult.PASS;

        var possibleRecipe = augment.fetchActiveRecipe();
        if (possibleRecipe.isEmpty()) return ActionResult.PASS;

        if (context.getWorld().isClient) return ActionResult.SUCCESS;

        var recipe = possibleRecipe.get();
        for (int height = 1; height <= 3; height++) {
            for (int width = 1; width <= 3; width++) {
                if (!recipe.value().fits(width, height)) continue;

                ItemOps.decrementPlayerHandItem(context.getPlayer(), context.getHand());
                context.getPlayer().getInventory().offerOrDrop(CarbonCopyItem.create(recipe, recipe.value().getResult(context.getWorld().getRegistryManager())));
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public Collection<Block> interactionOverrideCandidates(World world) {
        return List.of(AffinityBlocks.ASSEMBLY_AUGMENT);
    }
}
