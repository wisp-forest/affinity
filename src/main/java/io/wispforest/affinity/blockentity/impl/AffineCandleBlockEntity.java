package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AffineCandleBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    private static final Vec3d LINK_ATTACHMENT_POINT = new Vec3d(0, -.35f, 0);

    public AffineCandleBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AFFINE_CANDLE, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxInsert(256);
    }

    @Override
    public void tickServer() {
        if (!this.getCachedState().get(Properties.LIT)) return;

        long flux = this.fluxStorage.flux();
        if (flux > 0) this.updateFlux(flux - 1);

        if (this.getCachedState().get(CandleBlock.CANDLES) < 3) return;
        if (this.world.getTime() % 80 != 0) return;
        if (flux <= 50) return;

        var component = AffinityComponents.CHUNK_AETHUM.get(this.world.getChunk(this.pos));

        // TODO: this really needs to be enforced by the chunk itself
        if (component.getAethum() >= 100) return;
        component.setAethum(component.getAethum() + 0.1);

        this.updateFlux(flux - 50);
    }

    @Override
    public Vec3d linkAttachmentPointOffset() {
        return LINK_ATTACHMENT_POINT;
    }
}
