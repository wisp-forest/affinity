package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class RitualCoreBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity {

    @Nullable private RitualConfiguration cachedConfiguration = null;
    private int ritualTick = -1;
    private int lastActivatedSocle = -1;

    public RitualCoreBlockEntity(BlockEntityType<? extends RitualCoreBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Implemented by subclasses to indicate if an
     * additional check to any socles being
     * present succeeded
     *
     * @return {@code true} if the ritual may start
     * according to the checked conditions
     */
    protected abstract boolean checkRitualPreconditions();

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
        if (!this.checkRitualPreconditions()) return ActionResult.PASS;
        if (this.world.isClient()) return ActionResult.SUCCESS;

        var configuration = examineConfiguration((ServerWorld) this.world, this.pos);
        if (configuration.isEmpty()) return ActionResult.PASS;

        this.cachedConfiguration = configuration;
        Collections.shuffle(this.cachedConfiguration.socles(), this.world.random);

        this.ritualTick = 0;
        return ActionResult.SUCCESS;
    }

    @Override
    public void tickServer() {
        if (this.ritualTick < 0) return;

        if (this.ritualTick % 5 == 0 && ++this.lastActivatedSocle < this.cachedConfiguration.socles().size()) {
            var entity = this.world.getBlockEntity(this.cachedConfiguration.socles().get(this.lastActivatedSocle).position());
            if (entity instanceof RitualSocleBlockEntity socle) socle.beginExtraction(this.pos);
        }

        this.doRitualTick();

        if (this.ritualTick++ >= this.cachedConfiguration.length()) {
            this.ritualTick = -1;
            this.lastActivatedSocle = -1;
            this.cachedConfiguration = null;

            if (this.onRitualCompleted()) {
                this.markDirty();
            }
        }
    }

    public static RitualConfiguration examineConfiguration(RitualCoreBlockEntity core) {
        return examineConfiguration((ServerWorld) core.world, core.ritualCenterPos());
    }

    public static RitualConfiguration examineConfiguration(ServerWorld world, BlockPos pos) {
        var soclePOIs = world.getPointOfInterestStorage().getInCircle(type -> type == AffinityPoiTypes.RITUAL_SOCLE,
                pos, 10, PointOfInterestStorage.OccupationStatus.ANY).filter(poi -> poi.getPos().getY() == pos.getY()).toList();

        double stability = 75;

        List<Double> allDistances = new ArrayList<>((soclePOIs.size() - 1) * soclePOIs.size());

        var socles = new ArrayList<RitualSocleEntry>();
        for (var soclePOI : soclePOIs) {
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

        return new RitualConfiguration(stability, socles.size() * 5 + 40, socles);
    }

    public BlockPos ritualCenterPos() {
        return this.pos;
    }

    public record RitualConfiguration(double stability, int length, List<RitualSocleEntry> socles) {
        public boolean isEmpty() {
            return socles.isEmpty();
        }
    }

    public record RitualSocleEntry(BlockPos position, double meanDistance, double minDistance, double coreDistance) {}
}
