package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.particle.ColoredFallingDustParticleEffect;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FieldCoherenceModulatorBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity, InteractableBlockEntity {

    @Environment(EnvType.CLIENT) public double spinSpeed = 1d;
    @Environment(EnvType.CLIENT) public double spin = 0d;

    public FieldCoherenceModulatorBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.FIELD_COHERENCE_MODULATOR, pos, state);

        this.fluxStorage.setMaxInsert(1024);
        this.fluxStorage.setFluxCapacity(128000);
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.getWorld().isClient) {
            AffinityNetwork.server(this).send(new InteractionPacket(this.pos));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void tickClient() {
        var time = this.world.getTime() + this.timeOffset();

        double x = Math.sin(Math.toRadians(time) * 15) * .5;
        double y = .05 + Math.cos(Math.toRadians(time) * 10) * .25;
        double z = Math.cos(Math.toRadians(time) * 15) * .5;

        ClientParticles.spawn(
                new ColoredFallingDustParticleEffect(MathUtil.rgbToVec3f(Color.ofHsv((float) (.75f + Math.sin(time / 36f) * .25f), .3f, 1f).rgb())),
                this.world,
                Vec3d.ofCenter(this.pos).add(x, y, z),
                0f
        );

        for (var offset : Direction.values()) {
            var neighborPos = this.pos.offset(offset);
            if (!(this.world.getBlockEntity(neighborPos) instanceof FieldCoherenceModulatorBlockEntity modulator) || modulator.spinSpeed >= this.spinSpeed) {
                continue;
            }

            modulator.spinSpeed = Math.max(1d, this.spinSpeed * .5);
        }
    }

    @Environment(EnvType.CLIENT)
    public int timeOffset() {
        return (int) (this.pos.asLong() % 25000);
    }

    static {
        AffinityNetwork.CHANNEL.registerClientbound(InteractionPacket.class, (message, access) -> {
            if (!(access.runtime().world.getBlockEntity(message.pos) instanceof FieldCoherenceModulatorBlockEntity fieldCoherenceModulator)) {
                return;
            }

            fieldCoherenceModulator.spinSpeed += 10d;
        });
    }

    public record InteractionPacket(BlockPos pos) {}
}
