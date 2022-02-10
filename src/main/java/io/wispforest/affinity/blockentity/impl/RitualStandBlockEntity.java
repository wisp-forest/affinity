package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.util.NbtUtil;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class RitualStandBlockEntity extends SyncedBlockEntity {

    private final String ITEM_KEY = "item";

    @NotNull private ItemStack item = ItemStack.EMPTY;

    public RitualStandBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.RITUAL_STAND, pos, state);
    }

    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        var playerStack = player.getStackInHand(hand);

        if (playerStack.isEmpty()) {
            if (this.item.isEmpty()) return ActionResult.PASS;
            if (this.world.isClient()) return ActionResult.SUCCESS;

            player.setStackInHand(hand, this.item.copy());
            this.item = ItemStack.EMPTY;

            this.markDirty();
        } else {
            if (this.world.isClient()) return ActionResult.SUCCESS;

            if (this.item.isEmpty()) {
                this.item = ItemOps.singleCopy(playerStack);
                ItemOps.decrementPlayerHandItem(player, hand);

                this.markDirty();
            } else {
                if (ItemOps.canStack(playerStack, this.item)) {
                    playerStack.increment(1);
                } else {
                    ItemScatterer.spawn(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.item);
                }

                this.item = ItemStack.EMPTY;
                this.markDirty();
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.item = NbtUtil.readItemStack(nbt, ITEM_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtUtil.writeItemStack(nbt, ITEM_KEY, this.item);
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = item;
    }
}
