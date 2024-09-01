package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.format.nbt.NbtEndec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.ChestType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;

public class MangroveBasketBlockEntity extends SyncedBlockEntity {

    // TODO: move these to data components
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

    public ItemStack toItem(RegistryWrapper.WrapperLookup registries) {
        var stack = new ItemStack(AffinityBlocks.MANGROVE_BASKET);
        var nbt = new NbtCompound();
        var ctx = SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries));

        if (this.containedState != null) {
            nbt.put(ctx, CONTAINED_STATE_KEY, this.containedState);
        }

        if (this.containedBlockEntity != null) {
            nbt.put(ctx, CONTAINED_BLOCK_ENTITY_KEY, this.containedBlockEntity.createNbtWithId(registries));
        }

        nbt.putString("id", BlockEntityType.getId(this.getType()).toString());
        stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));

        return stack;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        this.containedState = nbt.get(CONTAINED_STATE_KEY);

        this.containedBlockEntity = BlockEntity.createFromNbt(this.pos, this.containedState, nbt.get(CONTAINED_BLOCK_ENTITY_KEY), registries);
        this.containedBlockEntity.setWorld(world);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        var ctx = SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries));

        nbt.putIfNotNull(ctx, CONTAINED_STATE_KEY, this.containedState);

        if (this.containedBlockEntity != null) {
            nbt.put(ctx, CONTAINED_BLOCK_ENTITY_KEY, this.containedBlockEntity.createNbtWithId(registries));
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