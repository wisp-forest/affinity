package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
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

import java.util.ArrayList;
import java.util.List;

public class RitualCoreBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity {

    public RitualCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.RITUAL_CORE, pos, state);
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (this.world.isClient()) return ActionResult.SUCCESS;

        final var centerPos = this.pos.add(0, 1, 0);
        var standPOIs = ((ServerWorld) this.world).getPointOfInterestStorage().getInCircle(type -> type == AffinityPoiTypes.RITUAL_STAND,
                this.pos, 10, PointOfInterestStorage.OccupationStatus.ANY).filter(poi -> poi.getPos().getY() == this.pos.getY() + 1).toList();

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
            stands.add(new RitualStandEntry(standPos, meanDistance, minDistance, MathUtil.distance(standPos, centerPos)));
        }

        final double mean = MathUtil.mean(allDistances);
        final double standardDeviation = MathUtil.standardDeviation(mean, allDistances);
        final var distancePenalty = mean > 4.5 ? mean - 4.5 : 0;

        stability -= distancePenalty * 15;
        stability *= Math.min((mean / 75) + 1.5 / standardDeviation, 1.25);

        for (var stand : stands) {
            var color = DyeColor.RED;

            if (stand.coreDistance() < 1.5) {
                stability *= .5;
            } else if (stand.minDistance() < 1.25) {
                stability *= .975;
            } else {
                color = DyeColor.GREEN;
            }

            sendDebugParticles(stand.position(), player, color);
        }

        player.sendMessage(Text.of(stands.size() + " - " + MathUtil.rounded(stability, 2)), true);

        return ActionResult.SUCCESS;
    }

    private void sendDebugParticles(BlockPos standPos, PlayerEntity player, DyeColor color) {
        var packet = new ParticleS2CPacket(new ColoredFlameParticleEffect(color),
                false, standPos.getX() + .5, standPos.getY() + 1.2, standPos.getZ() + .5,
                .1f, .1f, .1f, 0, 10);

        ((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
    }

    private record RitualStandEntry(BlockPos position, double meanDistance, double minDistance, double coreDistance) {}
}
