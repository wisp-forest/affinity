package io.wispforest.affinity.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class AethumFluxCacheItem extends BlockItem {
    public AethumFluxCacheItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Nullable
    @Override
    public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
        if (context.getPlayer().isSneaking()) return super.getPlacementContext(context);

        var state = context.getWorld().getBlockState(BlockPos.ofFloored(context.getHitPos()));
        if (!state.isOf(this.getBlock())) return super.getPlacementContext(context);

        var newPos = new BlockPos.Mutable(context.getHitPos().x, context.getHitPos().y, context.getHitPos().z);
        while (context.getWorld().getBlockState(newPos).isOf(this.getBlock())) {
            newPos.move(Direction.UP);
        }

        return ItemPlacementContext.offset(context, newPos, Direction.DOWN);
    }

    @Override
    protected boolean checkStatePlacement() {
        return false;
    }
}
