package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.endec.CodecUtils;
import io.wispforest.affinity.endec.nbt.NbtEndec;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class MangroveBasketBlockEntity extends SyncedBlockEntity {

    public static final KeyedEndec<BlockState> CONTAINED_STATE_KEY = CodecUtils.toEndec(BlockState.CODEC).keyed("ContainedState", (BlockState) null);
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
            var newState = this.containedState;

            if (newState.contains(Properties.HORIZONTAL_FACING)) {
                newState = newState.with(Properties.HORIZONTAL_FACING, Direction.NORTH);
            }

            if (newState.contains(Properties.FACING)) {
                newState = newState.with(Properties.FACING, Direction.NORTH);
            }

            nbt.put(CONTAINED_STATE_KEY, newState);
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
        nbt.putIfNotNull(SerializationContext.empty(), CONTAINED_STATE_KEY, this.containedState);

        if (this.containedBlockEntity != null) {
            nbt.put(CONTAINED_BLOCK_ENTITY_KEY, this.containedBlockEntity.createNbtWithId());
        }
    }

    public void onPlaced(LivingEntity placer) {
        if (this.containedState == null) return;
        var newState = this.containedState;

        if (newState.contains(Properties.HORIZONTAL_FACING)) {
            newState = newState.with(Properties.HORIZONTAL_FACING, placer.getHorizontalFacing().getOpposite());
        }

        if (newState.contains(Properties.FACING)) {
            newState = newState.with(Properties.FACING, Direction.getEntityFacingOrder(placer)[0].getOpposite().getOpposite());
        }

        if (!this.containedState.equals(newState)) {
            this.containedState = newState;
            this.markDirty();
        }
    }
}