package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import java.util.Collection;
import java.util.List;

public class AnthracitePowderItem extends Item implements DirectInteractionHandler {

    public AnthracitePowderItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var blockEntity = context.getWorld().getBlockEntity(context.getBlockPos());
        if (!(blockEntity instanceof AssemblyAugmentBlockEntity augment)) return ActionResult.PASS;

        var recipe = augment.getCurrentCraftingRecipe();
        if (recipe.isEmpty()) return ActionResult.PASS;

        if (context.getWorld().isClient) return ActionResult.SUCCESS;

        context.getPlayer().getInventory().offerOrDrop(CarbonCopyItem.create(recipe.get().getIngredients(), recipe.get().getOutput(context.getWorld().getRegistryManager())));
        return ActionResult.SUCCESS;
    }

    @Override
    public Collection<Block> interactionOverrideCandidates() {
        return List.of(AffinityBlocks.ASSEMBLY_AUGMENT);
    }
}
