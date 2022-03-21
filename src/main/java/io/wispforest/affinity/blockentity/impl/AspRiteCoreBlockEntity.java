package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.misc.NbtKey;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
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

public class AspRiteCoreBlockEntity extends RitualCoreBlockEntity {

    private final NbtKey<ItemStack> ITEM_KEY = new NbtKey<>("item", NbtKey.Type.ITEM_STACK);
    @NotNull private ItemStack item = ItemStack.EMPTY;

    public AspRiteCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASP_RITE_CORE, pos, state);
    }

    @Override
    protected ActionResult handleNormalUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(hand).isOf(AffinityItems.WAND_OF_INQUIRY)) return ActionResult.PASS;
        if (this.world.isClient()) return ActionResult.SUCCESS;

        return InteractionUtil.handleSingleItemContainer(this.world, this.pos, player, hand,
                () -> this.item, stack -> this.item = stack, this::markDirty);
    }

    @Override
    protected boolean onRitualCompleted() {
        this.item = ItemStack.EMPTY;
        return true;
    }

    @Override
    protected boolean checkRitualPreconditions() {
        return !this.item.isEmpty();
    }

    @Override
    public void onBroken() {
        super.onBroken();
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.getItem());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.item = ITEM_KEY.get(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        ITEM_KEY.put(nbt, this.item);
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    @Override
    protected void doRitualTick() {}
}
