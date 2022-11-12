package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class MangroveBasketBlockEntity extends SyncedBlockEntity {
    private BlockState containedState = null;
    private BlockEntity containedBlockEntity = null;

    public MangroveBasketBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.MANGROVE_BASKET, pos, state);
    }

    public void init(BlockState state, BlockEntity blockEntity) {
        containedState = state;
        containedBlockEntity = blockEntity;

        if (containedState.contains(Properties.CHEST_TYPE))
            containedState = containedState.with(Properties.CHEST_TYPE, ChestType.SINGLE);

        containedBlockEntity.setWorld(world);
    }

    public BlockState getContainedState() {
        return containedState;
    }

    public BlockEntity getContainedBlockEntity() {
        return containedBlockEntity;
    }

    public ItemStack toItem() {
        ItemStack stack = new ItemStack(AffinityBlocks.MANGROVE_BASKET);

        NbtCompound nbt = new NbtCompound();

        if (containedState != null) {
            var newState = containedState;

            if (newState.contains(Properties.HORIZONTAL_FACING))
                newState = newState.with(Properties.HORIZONTAL_FACING, Direction.NORTH);

            if (newState.contains(Properties.FACING))
                newState = newState.with(Properties.FACING, Direction.NORTH);

            nbt.put("ContainedState", NbtHelper.fromBlockState(newState));
        }

        if (containedBlockEntity != null)
            nbt.put("ContainedBlockEntity", containedBlockEntity.createNbtWithId());

        BlockItem.setBlockEntityNbt(stack, this.getType(), nbt);

        return stack;
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

    public void onPlaced(LivingEntity placer) {
        if (containedState == null)
            return;

        var newState = containedState;

        if (newState.contains(Properties.HORIZONTAL_FACING))
            newState = newState.with(Properties.HORIZONTAL_FACING, placer.getHorizontalFacing().getOpposite());

        if (newState.contains(Properties.FACING))
            newState =
                newState.with(Properties.FACING, Direction.getEntityFacingOrder(placer)[0].getOpposite().getOpposite());

        if (!containedState.equals(newState)) {
            containedState = newState;
            markDirty();
        }
    }
}
