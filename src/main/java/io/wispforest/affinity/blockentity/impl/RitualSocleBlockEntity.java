package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.misc.util.BlockFinder;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterest;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@SuppressWarnings("UnusedReturnValue")
public class RitualSocleBlockEntity extends SyncedBlockEntity implements InteractableBlockEntity, TickedBlockEntity {

    public static final Vec3d PARTICLE_OFFSET = new Vec3d(.5, 1, .5);

    private final NbtKey<ItemStack> ITEM_KEY = new NbtKey<>("Item", NbtKey.Type.ITEM_STACK);

    @NotNull private ItemStack item = ItemStack.EMPTY;

    private int extractionTicks = 0;
    private int extractionDuration = -1;
    public final RitualLock<RitualCoreBlockEntity> ritualLock = new RitualLock<>();

    public RitualSocleBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.RITUAL_SOCLE, pos, state);
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
        final var emitDuration = duration - travelDuration;

        AffinityParticleSystems.DISSOLVE_ITEM.spawn(this.world, particleOrigin(this.pos),
                new AffinityParticleSystems.DissolveData(
                        this.getItem(),
                        Vec3d.ofCenter(corePosition, .8),
                        emitDuration,
                        travelDuration
                )
        );

//        WorldOps.playSound(this.world, this.pos, AffinitySoundEvents.BLOCK_RITUAL_SOCLE_ACTIVATE, SoundCategory.BLOCKS);

        this.extractionTicks = 1;
        this.extractionDuration = emitDuration;
    }

    public void stopExtraction(boolean destroyItem) {
        this.extractionTicks = 0;
        this.extractionDuration = -1;

        if (destroyItem && !this.item.isEmpty()) {
            this.item = ItemStack.EMPTY;
            this.markDirty();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private PointOfInterest closestCore(int radius) {
        return BlockFinder.findPoi(this.world, AffinityPoiTypes.RITUAL_CORE, this.pos, radius)
                .min(Comparator.comparingDouble(value -> this.pos.getSquaredDistance(value.getPos())))
                .orElse(null);
    }

    @Override
    public void tickServer() {
        if (this.extractionTicks < 1) return;
        if (this.extractionTicks++ < this.extractionDuration) return;

        this.stopExtraction(true);
    }

    public void onBroken() {
        if (this.ritualLock.isActive()) {
            this.ritualLock.holder().onSocleDestroyed(this.pos);
        }

        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.item);
    }

    public static Vec3d particleOrigin(BlockPos soclePosition) {
        return Vec3d.of(soclePosition).add(PARTICLE_OFFSET);
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
}
