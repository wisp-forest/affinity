package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.ops.WorldOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VoidBeaconBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    public static final TagKey<Block> SUPPORT_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Affinity.id("void_beacon_support_blocks"));
    private boolean active = false;

    public VoidBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.VOID_BEACON, pos, state);

        this.fluxStorage.setMaxInsert(512);
        this.fluxStorage.setFluxCapacity(128000);
    }

    @Override
    public void tickClient() {
        this.updateActiveState();
    }

    @Override
    public void tickServer() {
        this.updateActiveState();
        if (this.active && this.world.getTime() % 80 == 0) {
            WorldOps.playSound(this.world, this.pos, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS);
        }
    }

    private void updateActiveState() {
        if (this.world.getTime() % 40 != 0) return;

        for (var testPos : BlockPos.iterate(this.pos.add(-1, 1, -1), this.pos.add(1, 1, 1))) {
            if (!this.world.getBlockState(testPos).isIn(SUPPORT_BLOCKS)) {
                this.setActive(false);
                return;
            }
        }

        for (var testPos : BlockPos.iterate(this.pos.down(), new BlockPos(this.pos.getX(), this.world.getBottomY(), this.pos.getZ()))) {
            if (this.world.getBlockState(testPos).getOpacity(this.world, testPos) >= 15) {
                this.setActive(false);
                return;
            }
        }

        this.setActive(true);
    }

    private void setActive(boolean active) {
        if (!this.world.isClient && this.active != active) {
            WorldOps.playSound(
                    this.world, this.pos,
                    active ? SoundEvents.BLOCK_BEACON_ACTIVATE : SoundEvents.BLOCK_BEACON_DEACTIVATE,
                    SoundCategory.BLOCKS
            );
        }

        this.active = active;
    }

    public boolean active() {
        return this.active;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Vec3d applyTooltipOffset(Vec3d tooltipPos) {
        return tooltipPos.add(0, -.25, 0);
    }
}
