package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.affinity.particle.ColoredFlameParticleEffect;
import io.wispforest.affinity.util.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RitualCoreBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity {

    @Nullable RitualConfiguration cachedConfiguration = null;
    private int ritualTick = -1;
    private int lastActivatedStand = -1;

    public RitualCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.RITUAL_CORE, pos, state);
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (this.world.isClient()) return ActionResult.SUCCESS;

        if (player.isSneaking()) {
            var configuration = this.examineConfiguration();
            player.sendMessage(Text.of(configuration.stands().size() + " - " + MathUtil.rounded(configuration.stability(), 2)), true);
            return ActionResult.SUCCESS;
        } else {
            return tryStartRitual();
        }
    }

    public ActionResult tryStartRitual() {
        var configuration = this.examineConfiguration();
        if (configuration.isEmpty()) return ActionResult.PASS;

        this.cachedConfiguration = configuration;
        Collections.shuffle(this.cachedConfiguration.stands(), this.world.random);

        this.ritualTick = 0;
        return ActionResult.SUCCESS;
    }

    @Override
    public void tickServer() {
        if (this.ritualTick < 0) return;

        if (this.ritualTick % 5 == 0 && ++this.lastActivatedStand < this.cachedConfiguration.stands().size()) {
            var entity = this.world.getBlockEntity(this.cachedConfiguration.stands().get(this.lastActivatedStand).position());
            if (entity instanceof RitualStandBlockEntity stand) stand.beginExtraction(this.pos);
        }

        if (this.ritualTick++ >= this.cachedConfiguration.length()) {
            this.ritualTick = -1;
            this.lastActivatedStand = -1;
            this.cachedConfiguration = null;
        }
    }

    private RitualConfiguration examineConfiguration() {
        var standPOIs = ((ServerWorld) this.world).getPointOfInterestStorage().getInCircle(type -> type == AffinityPoiTypes.RITUAL_STAND,
                this.pos, 10, PointOfInterestStorage.OccupationStatus.ANY).filter(poi -> poi.getPos().getY() == this.pos.getY()).toList();

        double stability = 75;

        List<Double> allDistances = new ArrayList<>((standPOIs.size() - 1) * standPOIs.size());

        var stands = new ArrayList<RitualStandEntry>();
        for (var standPOI : standPOIs) {
            double meanDistance = 0;
            double minDistance = Double.MAX_VALUE;

            final var standPos = standPOI.getPos();
            for (var other : standPOIs) {
                if (other == standPOI) continue;

                final var distance = MathUtil.distance(standPos, other.getPos());

                meanDistance += distance;
                allDistances.add(distance);
                if (distance < minDistance) minDistance = distance;
            }

            meanDistance /= (standPOIs.size() - 1d);
            stands.add(new RitualStandEntry(standPos, meanDistance, minDistance, MathUtil.distance(standPos, this.pos)));
        }

        final double mean = MathUtil.mean(allDistances);
        final double standardDeviation = MathUtil.standardDeviation(mean, allDistances);
        final var distancePenalty = mean > 4.5 ? mean - 4.5 : 0;

        stability -= distancePenalty * 15;
        stability *= Math.min((mean / 75) + 1.5 / standardDeviation, 1.25);

        for (var stand : stands) {
            if (stand.coreDistance() < 1.5) {
                stability *= .5;
            } else if (stand.minDistance() < 1.25) {
                stability *= .975;
            }
        }

        return new RitualConfiguration(stability, stands.size() * 5 + 50, stands);
    }

    private void sendDebugParticles(BlockPos standPos, PlayerEntity player, DyeColor color) {
        var packet = new ParticleS2CPacket(new ColoredFlameParticleEffect(color),
                false, standPos.getX() + .5, standPos.getY() + 1.2, standPos.getZ() + .5,
                .1f, .1f, .1f, 0, 10);

        ((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
    }

    private record RitualConfiguration(double stability, int length, List<RitualStandEntry> stands) {
        public boolean isEmpty() {
            return stands.isEmpty();
        }
    }

    private record RitualStandEntry(BlockPos position, double meanDistance, double minDistance, double coreDistance) {}
}
