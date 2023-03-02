package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CropReaperBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity, InquirableOutlineProvider {

    public CropReaperBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.CROP_REAPER, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxExtract(256);
    }

    @Override
    public void tickServer() {
        if (this.world.getTime() % 20 != 0) return;

        if (this.fluxStorage.flux() >= this.fluxStorage.fluxCapacity()) return;

        int addedAethum = 0;

        for (BlockPos pos : BlockPos.iterate(this.getPos().add(-8, -4, -8), this.pos.add(8, 4, 8))) {
            final var state = world.getBlockState(pos);

            if (!(state.getBlock() instanceof CropBlock crop)) continue;

            if (crop.isMature(state)) {
                world.breakBlock(pos, false);
                world.setBlockState(pos, crop.withAge(0));

                addedAethum += 200;
            }
        }

        if (addedAethum > 0) {
            this.updateFlux(Math.min(this.fluxStorage.flux() + addedAethum, this.fluxStorage.fluxCapacity()));
        }
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        return CuboidRenderer.Cuboid.symmetrical(8, 4, 8);
    }
}
