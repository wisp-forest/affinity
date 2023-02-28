package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.item.StaffItem;
import io.wispforest.affinity.misc.SingleElementDefaultedList;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.util.ImplementedInventory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StaffPedestalBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity, ImplementedInventory, SidedInventory {

    private static final int[] AVAILABLE_SLOTS = new int[]{0};
    private static final NbtKey<ItemStack> ITEM_KEY = new NbtKey<>("Item", NbtKey.Type.ITEM_STACK);

    @NotNull private ItemStack item = ItemStack.EMPTY;
    private int time = 0;

    private final SingleElementDefaultedList<ItemStack> inventoryProvider = new SingleElementDefaultedList<>(
            ItemStack.EMPTY, () -> this.item, stack -> this.item = stack
    );

    public StaffPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.STAFF_PEDESTAL, pos, state);
        this.fluxStorage.setFluxCapacity(16000);
        this.fluxStorage.setMaxInsert(4000);
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
        this.time++;

        if (this.item.isEmpty() || !(this.item.getItem() instanceof StaffItem staff)) return;
        staff.pedestalTickServer((ServerWorld) this.world, this.pos, this);
    }

    @Override
    public void tickClient() {
        this.time++;

        if (this.item.isEmpty() || !(this.item.getItem() instanceof StaffItem staff)) return;
        staff.pedestalTickClient(this.world, this.pos, this);
    }

    public @NotNull ItemStack getItem() {
        return this.item;
    }

    public int time() {
        return this.time;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasFlux(long flux) {
        return this.fluxStorage.flux() >= flux;
    }

    public void consumeFlux(long flux) {
        this.updateFlux(this.flux() - flux);
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);

        if (this.item.isEmpty() || !(this.item.getItem() instanceof StaffItem staff)) return;
        staff.appendTooltipEntries(this.world, this.pos, this, entries);
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

    @Override
    public void onBroken() {
        super.onBroken();
        ItemScatterer.spawn(this.world, this.pos, this.inventoryProvider);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventoryProvider;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return AVAILABLE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.getItem() instanceof StaffItem staff && staff.canBePlacedOnPedestal();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
