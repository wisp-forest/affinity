package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import io.wispforest.owo.serialization.format.nbt.NbtEndec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;

public class MangroveBasketBlockEntity extends SyncedBlockEntity {

    public static final KeyedEndec<BlockState> CONTAINED_STATE_KEY = Endec.ofCodec(BlockState.CODEC).keyed("ContainedState", (BlockState) null);
    public static final KeyedEndec<NbtCompound> CONTAINED_BLOCK_ENTITY_KEY = NbtEndec.COMPOUND.keyed("ContainedBlockEntity", (NbtCompound) null);

    private BlockState containedState = null;
    private BlockEntity containedBlockEntity = null;

    public MangroveBasketBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.MANGROVE_BASKET, pos, state);
    }

    public void init(BlockState state, BlockEntity blockEntity) {
        this.containedState = state;
        this.containedBlockEntity = blockEntity;

        if (this.containedState.contains(Properties.CHEST_TYPE)) {
            this.containedState = this.containedState.with(Properties.CHEST_TYPE, ChestType.SINGLE);
        }

        this.containedBlockEntity.setWorld(world);
    }

    public BlockState containedState() {
        return this.containedState;
    }

    public BlockEntity containedBlockEntity() {
        return this.containedBlockEntity;
    }

    public ItemStack toItem() {
        var stack = new ItemStack(AffinityBlocks.MANGROVE_BASKET);
        var nbt = new NbtCompound();

        if (this.containedState != null) {
            nbt.put(CONTAINED_STATE_KEY, this.containedState);
        }

        if (this.containedBlockEntity != null) {
            nbt.put(CONTAINED_BLOCK_ENTITY_KEY, this.containedBlockEntity.createNbtWithId());
        }

        BlockItem.setBlockEntityNbt(stack, this.getType(), nbt);

        return stack;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.containedState = nbt.get(CONTAINED_STATE_KEY);

        this.containedBlockEntity = BlockEntity.createFromNbt(this.pos, this.containedState, nbt.get(CONTAINED_BLOCK_ENTITY_KEY));
        this.containedBlockEntity.setWorld(world);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putIfNotNull(CONTAINED_STATE_KEY, this.containedState);

        if (this.containedBlockEntity != null) {
            nbt.put(CONTAINED_BLOCK_ENTITY_KEY, this.containedBlockEntity.createNbtWithId());
        }
    }

    public void onPlaced(ItemPlacementContext ctx) {
        if (this.containedState == null) return;

        var newState = this.containedState;
        var placementState = this.containedState.getBlock().getPlacementState(ctx);

        newState = copyProperty(placementState, newState, Properties.HORIZONTAL_FACING);
        newState = copyProperty(placementState, newState, Properties.FACING);
        newState = copyProperty(placementState, newState, Properties.HORIZONTAL_AXIS);
        newState = copyProperty(placementState, newState, Properties.AXIS);
        newState = copyProperty(placementState, newState, Properties.ATTACHMENT);
        newState = copyProperty(placementState, newState, Properties.BLOCK_FACE);
        newState = copyProperty(placementState, newState, Properties.HOPPER_FACING);

        if (!this.containedState.equals(newState)) {
            this.containedState = newState;
            this.markDirty();
        }
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        if (from.contains(property)) {
            return to.with(property, from.get(property));
        }

        return to;
    }
}