package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.misc.NbtKey;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityPoiTypes;
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
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class RitualSocleBlockEntity extends SyncedBlockEntity implements InteractableBlockEntity, TickedBlockEntity {

    private final NbtKey<ItemStack> ITEM_KEY = new NbtKey<>("Item", NbtKey.Type.ITEM_STACK);

    @NotNull private ItemStack item = ItemStack.EMPTY;
    private int extractionTicks = 0;
    private int extractionDuration = -1;

    public RitualSocleBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.RITUAL_SOCLE, pos, state);
    }

    public void onBroken() {
        final var corePoi = this.closestCore(10);
        if (corePoi != null && this.world.getBlockEntity(corePoi.getPos()) instanceof RitualCoreBlockEntity core) {
            core.onSocleDestroyed(this.pos, this);
        }

        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.item);
    }

    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking()) {
            if (this.world.isClient) return ActionResult.SUCCESS;

            final var core = this.closestCore(10);

            if (core == null) return ActionResult.SUCCESS;
            this.beginExtraction(core.getPos(), 40);

            return ActionResult.SUCCESS;
        } else {
            return InteractionUtil.handleSingleItemContainer(this.world, this.pos, player, hand,
                    () -> this.item, stack -> this.item = stack, this::markDirty);
        }
    }

    public void beginExtraction(BlockPos corePosition, int duration) {
        final var travelDuration = Math.min(15, duration);
        AffinityParticleSystems.DISSOLVE_ITEM.spawn(this.world, Vec3d.of(this.pos).add(.5, 1, .5),
                new AffinityParticleSystems.DissolveData(this.getItem(), Vec3d.ofCenter(corePosition).add(0, .3, 0),
                        duration - travelDuration, travelDuration));
        this.extractionTicks = 1;
        this.extractionDuration = duration;
    }

    private PointOfInterest closestCore(int radius) {
        return ((ServerWorld) this.world).getPointOfInterestStorage()
                .getInCircle(type -> type == AffinityPoiTypes.RITUAL_CORE, this.pos, radius, PointOfInterestStorage.OccupationStatus.ANY)
                .min(Comparator.comparingDouble(value -> this.pos.getSquaredDistance(value.getPos()))).orElse(null);
    }

    @Override
    public void tickServer() {
        if (this.extractionTicks < 1) return;
        if (this.extractionTicks++ < this.extractionDuration) return;

        this.extractionTicks = 0;
        this.extractionDuration = -1;
        this.item = ItemStack.EMPTY;
        this.markDirty();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.item = ITEM_KEY.get(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        ITEM_KEY.put(nbt, this.item);
    }

    public @NotNull ItemStack getItem() {
        return item;
    }
}
