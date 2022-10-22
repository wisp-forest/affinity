package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

public class MangroveBasketBlockEntity extends SyncedBlockEntity {
    private BlockState containedState = null;
    private BlockEntity containedBlockEntity = null;

    public MangroveBasketBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.MANGROVE_BASKET, pos, state);
    }

    public void init(BlockState state, BlockEntity blockEntity) {
        containedState = state;
        containedBlockEntity = blockEntity;

        containedBlockEntity.setWorld(world);
    }

    public BlockState getContainedState() {
        return containedState;
    }

    public BlockEntity getContainedBlockEntity() {
        return containedBlockEntity;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        containedState = NbtHelper.toBlockState(nbt.getCompound("ContainedState"));
        containedBlockEntity = BlockEntity.createFromNbt(pos, containedState, nbt.getCompound("ContainedBlockEntity"));

        containedBlockEntity.setWorld(world);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (containedState != null)
            nbt.put("ContainedState", NbtHelper.fromBlockState(containedState));

        if (containedBlockEntity != null)
            nbt.put("ContainedBlockEntity", containedBlockEntity.createNbtWithId());
    }
}
