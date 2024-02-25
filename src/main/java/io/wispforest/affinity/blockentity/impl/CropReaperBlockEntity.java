package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class CropReaperBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity, InquirableOutlineProvider {

    private int time = 0;

    public CropReaperBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.CROP_REAPER, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxExtract(256);
    }

    @Override
    public void tickServer() {
        this.time++;
        if (this.time % 600 != 0 || this.flux() >= this.fluxCapacity()) return;

        int addedAethum = 0;

        for (var pos : BlockPos.iterate(this.pos.add(-8, 0, -8), this.pos.add(8, 2, 8))) {
            var state = this.world.getBlockState(pos);
            if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMature(state)) continue;

            this.world.setBlockState(pos, crop.withAge(0), Block.NOTIFY_LISTENERS);
            this.world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));

            addedAethum += 75;
        }

        if (addedAethum > 0) {
            this.updateFlux(Math.min(this.flux() + addedAethum, this.fluxCapacity()));
        }
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        return CuboidRenderer.Cuboid.of(new BlockPos(-8, 0, -8), new BlockPos(9, 3, 9));
    }
}
