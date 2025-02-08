package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.SonicSyphonBlock;
import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.misc.ServerTasks;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PitcherCropBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SonicSyphonBlockEntity extends SyncedBlockEntity implements TickedBlockEntity {

    private int time = 0;

    public SonicSyphonBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.SONIC_SYPHON, pos, state);
    }

    @Override
    public void tickServer() {
        this.time++;

        if (this.time % 30 == 0) {
            var facing = this.getCachedState().get(SonicSyphonBlock.FACING);

            var potentialPodPos = this.pos.offset(facing);
            var potentialPodState = this.world.getBlockState(potentialPodPos);
            if (this.getPitcherAge(potentialPodState, DoubleBlockHalf.LOWER) < 4) return;

            AffinityParticleSystems.DIRECTIONAL_SHRIEK.spawn(
                this.world,
                Vec3d.ofCenter(this.pos),
                facing
            );

            ServerTasks.doFor((ServerWorld) this.world, 24, (tick) -> {
                if (tick % 6 != 0) return true;

                int targetAge = 4 - (tick / 6);

                var testState = this.world.getBlockState(potentialPodPos);
                if (this.getPitcherAge(testState, DoubleBlockHalf.LOWER) == targetAge + 1) {
                    this.world.setBlockState(potentialPodPos, testState.with(PitcherCropBlock.AGE, targetAge));
                } else {
                    return false;
                }

                var upperTestState = this.world.getBlockState(potentialPodPos.up());
                if (targetAge >= 2) {
                    if (this.getPitcherAge(upperTestState, DoubleBlockHalf.UPPER) == targetAge + 1) {
                        if (targetAge >= 3) {
                            this.world.setBlockState(potentialPodPos.up(), upperTestState.with(PitcherCropBlock.AGE, targetAge));
                        } else {
                            this.world.removeBlock(potentialPodPos.up(), false);
                        }
                    } else {
                        return false;
                    }
                }

                return true;
            }, () -> {
                var spawnPos = Vec3d.ofCenter(potentialPodPos, .75);
                var item = new ItemEntity(this.world, spawnPos.x, spawnPos.y, spawnPos.z, AffinityItems.PITCHER_ELIXIR_BOTTLE.getDefaultStack());

                item.setVelocity(facing.getOffsetX() * .5, facing.getOffsetY() * .5, facing.getOffsetZ() * .5);
                this.world.spawnEntity(item);
            });
        }
    }

    private int getPitcherAge(BlockState state, DoubleBlockHalf half) {
        return state.getBlock() == Blocks.PITCHER_CROP && state.get(PitcherCropBlock.HALF) == half
            ? state.get(PitcherCropBlock.AGE)
            : -1;
    }
}
