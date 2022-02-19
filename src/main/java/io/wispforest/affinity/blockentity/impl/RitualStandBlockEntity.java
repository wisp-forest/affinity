package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.affinity.util.NbtUtil;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class RitualStandBlockEntity extends SyncedBlockEntity implements InteractableBlockEntity, TickedBlockEntity {

    private final String ITEM_KEY = "item";

    @NotNull private ItemStack item = ItemStack.EMPTY;
    private int extractionTicks = 0;

    public RitualStandBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.RITUAL_STAND, pos, state);
    }

    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking()) {
            if (this.world.isClient) return ActionResult.SUCCESS;

            final var core = ((ServerWorld) this.world).getPointOfInterestStorage()
                    .getInCircle(type -> type == AffinityPoiTypes.RITUAL_CORE, this.pos, 10, PointOfInterestStorage.OccupationStatus.ANY)
                    .min(Comparator.comparingDouble(value -> this.pos.getSquaredDistance(value.getPos())));

            if (core.isEmpty()) return ActionResult.SUCCESS;
            beginExtraction(core.get().getPos());

        } else {
            var playerStack = player.getStackInHand(hand);

            if (playerStack.isEmpty()) {
                if (this.item.isEmpty()) return ActionResult.PASS;
                if (this.world.isClient()) return ActionResult.SUCCESS;

                player.setStackInHand(hand, this.item.copy());
                this.item = ItemStack.EMPTY;

                this.markDirty();
            } else {
                if (this.world.isClient()) return ActionResult.SUCCESS;

                if (this.item.isEmpty()) {
                    this.item = ItemOps.singleCopy(playerStack);
                    ItemOps.decrementPlayerHandItem(player, hand);

                    this.markDirty();
                } else {
                    if (ItemOps.canStack(playerStack, this.item)) {
                        playerStack.increment(1);
                    } else {
                        ItemScatterer.spawn(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.item);
                    }

                    this.item = ItemStack.EMPTY;
                    this.markDirty();
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    public void beginExtraction(BlockPos corePosition) {
        AffinityParticleSystems.DISSOLVE_ITEM.spawn(this.world, Vec3d.of(this.pos).add(.5, 1, .5),
                new AffinityParticleSystems.DissolveData(this.getItem(), Vec3d.ofCenter(corePosition).add(0, .3, 0)));
        this.extractionTicks = 1;
    }

    @Override
    public void tickServer() {
        if (this.extractionTicks < 1) return;
        if (this.extractionTicks++ < 20) return;

        this.extractionTicks = 0;
        this.item = ItemStack.EMPTY;
        this.markDirty();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.item = NbtUtil.readItemStack(nbt, ITEM_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtUtil.writeItemStack(nbt, ITEM_KEY, this.item);
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = item;
    }
}
