package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.mixin.access.ChestBlockEntityAccessor;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class AzaleaChestBlockEntity extends ChestBlockEntity {

    public AzaleaChestBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(AffinityBlocks.Entities.AZALEA_CHEST, blockPos, blockState);

        ((ChestBlockEntityAccessor) this).affinity$setInventory(DefaultedList.ofSize(this.size(), ItemStack.EMPTY));
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, playerInventory, this, 5);
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.affinity.azalea_chest");
    }
}
