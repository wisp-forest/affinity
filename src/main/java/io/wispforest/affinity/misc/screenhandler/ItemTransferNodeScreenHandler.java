package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.blockentity.impl.ItemTransferNodeBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.SyncedProperty;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class ItemTransferNodeScreenHandler extends ScreenHandler {

    public final ItemTransferNodeBlockEntity node;
    public final SyncedProperty<ItemStack> filterStack;

    public final SyncedProperty<Boolean> ignoreDamage;
    public final SyncedProperty<Boolean> ignoreData;
    public final SyncedProperty<Boolean> invertFilter;

    public static ItemTransferNodeScreenHandler client(int syncId, PlayerInventory playerInventory) {
        return new ItemTransferNodeScreenHandler(syncId, playerInventory, null);
    }

    public ItemTransferNodeScreenHandler(int syncId, PlayerInventory inventory, ItemTransferNodeBlockEntity node) {
        super(AffinityScreenHandlerTypes.ITEM_TRANSFER_NODE, syncId);

        this.node = node;
        this.filterStack = this.createProperty(ItemStack.class, ItemStack.EMPTY);

        this.ignoreDamage = this.createProperty(Boolean.class, false);
        this.ignoreData = this.createProperty(Boolean.class, false);
        this.invertFilter = this.createProperty(Boolean.class, false);

        if (this.node != null) {
            this.filterStack.set(this.node.filterStack());

            this.ignoreDamage.set(node.ignoreDamage);
            this.ignoreData.set(node.ignoreData);
            this.invertFilter.set(node.invertFilter);
        }

        this.addServerboundMessage(RequestFilterUpdateMessage.class, message -> this.updateFilterStack());
        this.addServerboundMessage(RequestFilterConfigurationUpdateMessage.class, message -> this.updateFilterConfiguration(message.ignoreDamage, message.ignoreData, message.invert));

        SlotGenerator.begin(this::addSlot, 8, 110).playerInventory(inventory);
    }

    public void updateFilterStack() {
        if (this.node == null) {
            this.sendMessage(new RequestFilterUpdateMessage());
        } else {
            var stack = this.getCursorStack();
            if (stack.isEmpty()) stack = ItemStack.EMPTY;

            this.node.setFilterStack(stack);
            this.filterStack.set(stack);
        }
    }

    public void updateFilterConfiguration(boolean ignoreDamage, boolean ignoreData, boolean invert) {
        if (this.node == null) {
            this.sendMessage(new RequestFilterConfigurationUpdateMessage(ignoreDamage, ignoreData, invert));
        } else {
            this.node.ignoreDamage = ignoreDamage;
            this.node.ignoreData = ignoreData;
            this.node.invertFilter = invert;
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ScreenUtils.handleSlotTransfer(this, slot, 0);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (this.node == null) return false;
        return this.node.getWorld().getBlockState(this.node.getPos()).isOf(AffinityBlocks.ITEM_TRANSFER_NODE);
    }

    public record RequestFilterUpdateMessage() {}

    public record RequestFilterConfigurationUpdateMessage(boolean ignoreDamage, boolean ignoreData, boolean invert) {}
}
