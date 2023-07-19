package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.AethumProbeBlock;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class AethumProbeBlockEntity extends BlockEntity implements TickedBlockEntity {

    public AethumProbeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_PROBE, pos, state);
    }

    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);

        if (this.world == null || this.world.isClient) return;
        this.updateRedstoneState();
    }

    @Override
    public void tickServer() {
        if (this.world.getTime() % 50 != 0) return;
        this.updateRedstoneState();
    }

    private void updateRedstoneState() {
        var aethum = this.world.getChunk(this.pos).getComponent(AffinityComponents.CHUNK_AETHUM).aethumAt(this.pos.getX(), this.pos.getZ());
        var threshold = 90 - this.getCachedState().get(AethumProbeBlock.CRYSTALS) * 20;

        boolean shouldBePowered = aethum >= threshold;
        if (this.getCachedState().get(AethumProbeBlock.POWERED) == shouldBePowered) return;

        this.world.setBlockState(this.pos, this.getCachedState().with(AethumProbeBlock.POWERED, shouldBePowered));
        AethumProbeBlock.updateNeighbors(this.world, this.pos);
    }
}
