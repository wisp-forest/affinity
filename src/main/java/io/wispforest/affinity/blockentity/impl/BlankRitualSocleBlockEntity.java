package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.*;
import io.wispforest.affinity.misc.SingleStackStorageProvider;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnusedReturnValue")
public class BlankRitualSocleBlockEntity extends SyncedBlockEntity implements InteractableBlockEntity, ItemSocleBlockEntity {

    private static final KeyedEndec<ItemStack> ITEM_KEY = BuiltInEndecs.ITEM_STACK.keyed("Item", ItemStack.EMPTY);

    @NotNull
    private ItemStack item = ItemStack.EMPTY;
    private final SingleStackStorageProvider storageProvider = new SingleStackStorageProvider(() -> this.item, stack -> this.item = stack, this::markDirty).capacity(1);

    public BlankRitualSocleBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.BLANK_RITUAL_SOCLE, pos, state);
    }

    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (this.world.isClient) return ActionResult.SUCCESS;

        return InteractionUtil.handleSingleItemContainer(this.world, this.pos, player, hand,
                () -> this.item, stack -> this.item = stack, this::markDirty);
    }

    public void onBroken() {
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.item);
    }
    @Override
    public void readNbt(NbtCompound nbt) {
        this.item = nbt.get(ITEM_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.put(ITEM_KEY, this.item);
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    static {
        //noinspection UnstableApiUsage
        ItemStorage.SIDED.registerForBlockEntity((socle, direction) -> socle.storageProvider, AffinityBlocks.Entities.BLANK_RITUAL_SOCLE);
    }
}
