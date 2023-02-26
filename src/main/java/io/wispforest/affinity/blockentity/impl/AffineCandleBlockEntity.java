package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.AffineCandleBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

public class AffineCandleBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    private static final Vec3d LINK_ATTACHMENT_POINT = new Vec3d(0, -.35f, 0);

    private int time = ThreadLocalRandom.current().nextInt(100);

    public AffineCandleBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AFFINE_CANDLE, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxInsert(256);
    }

    @Override
    public void tickServer() {
        this.time++;
        if (!this.getCachedState().get(AffineCandleBlock.LIT)) return;

        long flux = this.flux();
        if (flux > 0) this.updateFlux(flux - 1);

        if (this.getCachedState().get(AffineCandleBlock.CANDLES) < 3) return;
        if (this.time % 80 != 0) return;
        if (flux <= 50) return;

        final var component = AffinityComponents.CHUNK_AETHUM.get(this.world.getChunk(this.pos));
        if (component.getAethum() >= 99.9) return;

        component.addAethum(0.1);
        this.updateFlux(flux - 50);
    }

    @Override
    public Vec3d linkAttachmentPointOffset() {
        return LINK_ATTACHMENT_POINT;
    }
}
