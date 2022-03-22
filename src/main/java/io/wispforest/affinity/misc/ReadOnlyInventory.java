package io.wispforest.affinity.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ReadOnlyInventory extends Inventory {

    @Override
    @Deprecated
    default ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    default ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    default boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    @Deprecated
    default void setStack(int slot, ItemStack stack) {}

    @Override
    @Deprecated
    default void markDirty() {}

    @Override
    @Deprecated
    default void clear() {}

    interface ListBacked extends ReadOnlyInventory {
        List<ItemStack> delegate();

        @Override
        default int size() {
            return this.delegate().size();
        }

        @Override
        default boolean isEmpty() {
            return this.delegate().isEmpty();
        }

        @Override
        default ItemStack getStack(int slot) {
            return this.delegate().get(slot);
        }
    }

}
