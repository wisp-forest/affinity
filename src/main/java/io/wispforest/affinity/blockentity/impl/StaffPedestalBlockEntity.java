package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.item.StaffItem;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class StaffPedestalBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity {

    private final NbtKey<ItemStack> ITEM_KEY = new NbtKey<>("Item", NbtKey.Type.ITEM_STACK);

    @NotNull private ItemStack item = ItemStack.EMPTY;

    public StaffPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.STAFF_PEDESTAL, pos, state);
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractionUtil.handleSingleItemContainer(
                world, pos, player, hand,
                stack -> stack.getItem() instanceof StaffItem staff && staff.canBePlacedOnPedestal(),
                InteractionUtil.InvalidBehaviour.DROP,
                () -> this.item,
                stack -> this.item = stack,
                this::markDirty
        );
    }

    @Override
    public void tickServer() {
        if (this.item.isEmpty() || !(this.item.getItem() instanceof StaffItem staff)) return;
        staff.pedestalTickServer(this.world, this.pos, this);
    }

    @Override
    public void tickClient() {
        if (this.item.isEmpty() || !(this.item.getItem() instanceof StaffItem staff)) return;
        staff.pedestalTickClient(this.world, this.pos, this);
    }

    public @NotNull ItemStack getItem() {
        return this.item;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.item = nbt.get(ITEM_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put(ITEM_KEY, this.item);
    }
}
