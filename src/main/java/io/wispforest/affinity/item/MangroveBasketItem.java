package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.MangroveBasketBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.*;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class MangroveBasketItem extends BlockItem {
    public static final TagKey<Block> MANGROVE_BASKET_BLACKLIST = TagKey.of(Registry.BLOCK_KEY, Affinity.id("mangrove_basket_blacklist"));

    public MangroveBasketItem() {
        super(AffinityBlocks.MANGROVE_BASKET, AffinityItems.settings(AffinityItemGroup.MAIN));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (BlockItem.getBlockEntityNbt(context.getStack()) != null) {
            return this.place(new ItemPlacementContext(context));
        } else {
            return this.place(new BasketPlacementContext(context));
        }
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(ItemPlacementContext context) {
        if (BlockItem.getBlockEntityNbt(context.getStack()) == null) {
            var currentState = context.getWorld().getBlockState(context.getBlockPos());

            if (currentState.isIn(MANGROVE_BASKET_BLACKLIST))
                return null;

            if (context.getWorld().getBlockEntity(context.getBlockPos()) == null)
                return null;
        }

        return super.getPlacementState(context);
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        var nbt = BlockItem.getBlockEntityNbt(context.getStack());

        if (nbt != null) {
            return super.place(context, state);
        }

        BlockState old = context.getWorld().getBlockState(context.getBlockPos());
        BlockEntity oldBlockEntity = context.getWorld().getBlockEntity(context.getBlockPos());

        context.getWorld().removeBlockEntity(context.getBlockPos());

        if (!context.getWorld().setBlockState(context.getBlockPos(), state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD))
            return false;

        if (context.getWorld().getBlockEntity(context.getBlockPos()) instanceof MangroveBasketBlockEntity newBlockEntity) {
            newBlockEntity.init(old, oldBlockEntity);
            return true;
        } else {
            return false;
        }
    }

    private static class BasketPlacementContext extends ItemPlacementContext {
        public BasketPlacementContext(ItemUsageContext context) {
            super(context);

            this.canReplaceExisting = true;
        }
    }
}
