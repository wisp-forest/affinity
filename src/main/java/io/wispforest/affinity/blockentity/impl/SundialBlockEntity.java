package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SundialBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    private static final Vec3d LINK_ATTACHMENT_POINT = new Vec3d(0, -.4f, 0);

    public SundialBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.SUNDIAL, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxExtract(256);
    }

    @Override
    public void tickServer() {
        var flux = this.fluxStorage.flux();
        if (flux >= this.fluxStorage.fluxCapacity()) return;

        this.updateFlux(Math.min(flux + 20, this.fluxStorage.fluxCapacity()));
    }

    @Override
    public Vec3d linkAttachmentPointOffset() {
        return LINK_ATTACHMENT_POINT;
    }
}
