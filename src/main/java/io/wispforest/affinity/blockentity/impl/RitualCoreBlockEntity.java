package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.affinity.particle.ColoredFlameParticleEffect;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;

public class RitualCoreBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity {

    public RitualCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.RITUAL_CORE, pos, state);
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (this.world.isClient()) return ActionResult.SUCCESS;

        var stands = ((ServerWorld) this.world).getPointOfInterestStorage().getInCircle(type -> type == AffinityPoiTypes.RITUAL_STAND,
                this.pos, 10, PointOfInterestStorage.OccupationStatus.ANY).toList();

        for (var stand : stands) {
            var standPos = stand.getPos();

            var packet = new ParticleS2CPacket(new ColoredFlameParticleEffect(DyeColor.byId(world.random.nextInt(16))),
                    false, standPos.getX() + .5, standPos.getY() + 1.2, standPos.getZ() + .5,
                    .1f, .1f, .1f, 0, 10);

            ((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
        }

        return ActionResult.SUCCESS;
    }
}
