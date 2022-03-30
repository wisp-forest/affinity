package io.wispforest.affinity.blockentity.template;

import io.wispforest.affinity.blockentity.impl.RitualSocleBlockEntity;
import io.wispforest.affinity.misc.ReadOnlyInventory;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class RitualCoreBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity {

    @Nullable private RitualSetup cachedSetup = null;
    private int ritualTick = -1;
    private int lastActivatedSocle = -1;

    public RitualCoreBlockEntity(BlockEntityType<? extends RitualCoreBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Called when a ritual is about to start
     *
     * @return {@code true} if the ritual can
     * start given the current conditions
     */
    protected abstract boolean onRitualStart(RitualSetup setup);

    /**
     * Called every tick during a ritual, may well do nothing
     */
    protected abstract void doRitualTick();

    /**
     * Called when a ritual has finished
     *
     * @return {@code true} if state was modified
     * and {@link #markDirty()} should be called
     */
    protected abstract boolean onRitualCompleted();

    /**
     * Called when a ritual is interrupted, usually
     * because of a socle being removed
     *
     * @return {@code true} if state was modified
     * and {@link #markDirty()} should be called
     */
    protected abstract boolean onRitualInterrupted();

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking()) {
            return this.tryStartRitual();
        } else {
            return this.handleNormalUse(player, hand, hit);
        }
    }

    protected ActionResult handleNormalUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    public ActionResult tryStartRitual() {
        if (this.world.isClient()) return ActionResult.SUCCESS;

        var setup = examineSetup((ServerWorld) this.world, this.pos, false);
        if (setup.isEmpty()) return ActionResult.PASS;

        if (!this.onRitualStart(setup)) return ActionResult.PASS;
        if (setup.duration() < 0) throw new IllegalStateException("No ritual length was configured. If you're a player, report this issue");

        this.cachedSetup = setup;
        Collections.shuffle(this.cachedSetup.socles, this.world.random);

        this.cachedSetup.forEachSocle(world, socle -> socle.acquireRitualLock(this));

        this.ritualTick = 0;
        return ActionResult.SUCCESS;
    }

    @Override
    public void tickServer() {
        if (this.ritualTick < 0) return;

        if (this.cachedSetup.isSocleActivationTick(this.ritualTick) && ++this.lastActivatedSocle < this.cachedSetup.socles.size()) {
            var entity = this.world.getBlockEntity(this.cachedSetup.socles.get(this.lastActivatedSocle).position());
            if (entity instanceof RitualSocleBlockEntity socle) socle.beginExtraction(this.pos, this.cachedSetup.durationPerSocle());
        }

        this.doRitualTick();

        if (this.ritualTick++ >= this.cachedSetup.duration()) {
            this.doPostRunCleanup(this::onRitualCompleted);
        }
    }

    public void onSocleDestroyed(BlockPos pos) {
        if (this.cachedSetup == null) return;
        if (!this.cachedSetup.hasSocleAt(pos)) return;

        this.onRitualInterrupted();

        this.doPostRunCleanup(this::onRitualInterrupted);
    }

    private void doPostRunCleanup(Supplier<Boolean> handlerImpl) {
        this.ritualTick = -1;
        this.lastActivatedSocle = -1;

        this.cachedSetup.forEachSocle(this.world, RitualSocleBlockEntity::releaseRitualLock);
        this.cachedSetup = null;

        if (handlerImpl.get()) {
            this.markDirty();
        }
    }

    public BlockPos ritualCenterPos() {
        return this.pos;
    }

    public static RitualSetup examineSetup(RitualCoreBlockEntity core, boolean includeEmptySocles) {
        return examineSetup((ServerWorld) core.world, core.ritualCenterPos(), includeEmptySocles);
    }

    @SuppressWarnings("ConstantConditions")
    public static RitualSetup examineSetup(ServerWorld world, BlockPos pos, boolean includeEmptySocles) {
        var soclePOIs = world.getPointOfInterestStorage().getInCircle(type -> type == AffinityPoiTypes.RITUAL_SOCLE,
                pos, 10, PointOfInterestStorage.OccupationStatus.ANY).filter(poi -> poi.getPos().getY() == pos.getY()).toList();

        double stability = 75;

        List<Double> allDistances = new ArrayList<>((soclePOIs.size() - 1) * soclePOIs.size());

        var socles = new ArrayList<RitualSocleEntry>();
        for (var soclePOI : soclePOIs) {

            if (!(world.getBlockEntity(soclePOI.getPos()) instanceof RitualSocleBlockEntity socle)) continue;
            if (socle.isLocked()) continue;

            if (!includeEmptySocles) {
                if (socle.getItem().isEmpty()) continue;
            }

            double meanDistance = 0;
            double minDistance = Double.MAX_VALUE;

            final var soclePos = soclePOI.getPos();
            for (var other : soclePOIs) {
                if (other == soclePOI) continue;

                final var distance = MathUtil.distance(soclePos, other.getPos());

                meanDistance += distance;
                allDistances.add(distance);
                if (distance < minDistance) minDistance = distance;
            }

            meanDistance /= (soclePOIs.size() - 1d);
            socles.add(new RitualSocleEntry(soclePos, meanDistance, minDistance, MathUtil.distance(soclePos, pos)));
        }

        if (allDistances.isEmpty()) allDistances.add(0d);

        final double mean = MathUtil.mean(allDistances);
        final double standardDeviation = MathUtil.standardDeviation(mean, allDistances);
        final var distancePenalty = mean > 4.5 ? mean - 4.5 : 0;

        stability -= distancePenalty * 15;
        stability *= Math.min((mean / 75) + 1.5 / standardDeviation, 1.25);

        for (var socle : socles) {
            if (socle.coreDistance() < 1.5) {
                stability *= .5;
            } else if (socle.minDistance() < 1.25) {
                stability *= .975;
            } else {
                final var socleType = RitualSocleType.forBlockState(world.getBlockState(socle.position()));
                stability += (100 - stability) * (socleType == null ? 0 : socleType.stabilityModifier());
            }
        }

        return new RitualSetup(stability, socles);
    }

    public static final class RitualSetup {
        public final double stability;
        public final List<RitualSocleEntry> socles;

        private int duration = -1;
        private int durationPerSocle = -1;
        private int socleDelay = -1;

        public RitualSetup(double stability, List<RitualSocleEntry> socles) {
            this.stability = stability;
            this.socles = socles;
        }

        public boolean isEmpty() {
            return socles.isEmpty();
        }

        public int duration() {
            return this.duration;
        }

        public int durationPerSocle() {
            return this.durationPerSocle;
        }

        public boolean isSocleActivationTick(int tick) {
            return tick % this.socleDelay == 0;
        }

        public boolean hasSocleAt(BlockPos pos) {
            return this.socles.stream().anyMatch(ritualSocleEntry -> ritualSocleEntry.position().equals(pos));
        }

        public void configureLength(int length) {
            this.duration = length;

            this.socleDelay = (int) Math.floor(length * (1 / (this.socles.size() + 2f)));
            this.durationPerSocle = length - this.socleDelay * (this.socles.size() - 1);
        }

        public List<RitualSocleBlockEntity> resolveSocles(World world) {
            var socleEntities = new ArrayList<RitualSocleBlockEntity>(this.socles.size());
            for (var entry : this.socles) {
                socleEntities.add((RitualSocleBlockEntity) world.getBlockEntity(entry.position()));
            }
            return socleEntities;
        }

        public void forEachSocle(World world, Consumer<RitualSocleBlockEntity> action) {
            for (var socle : this.resolveSocles(world)) action.accept(socle);
        }

    }

    public record RitualSocleEntry(BlockPos position, double meanDistance, double minDistance, double coreDistance) {}

    public static class SocleInventory implements ReadOnlyInventory.ListBacked {

        protected final DefaultedList<ItemStack> items;

        public SocleInventory(List<RitualSocleBlockEntity> socles) {
            this.items = DefaultedList.ofSize(socles.size(), ItemStack.EMPTY);

            for (int i = 0; i < socles.size(); i++) {
                this.items.set(i, socles.get(i).getItem());
            }
        }

        @Override
        public List<ItemStack> delegate() {
            return this.items;
        }
    }

}
