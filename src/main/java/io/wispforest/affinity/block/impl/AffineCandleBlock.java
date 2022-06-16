package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.blockentity.impl.AffineCandleBlockEntity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.particle.SmallColoredFlameParticleEffect;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AffineCandleBlock extends CandleBlock implements BlockEntityProvider {
    public AffineCandleBlock() {
        super(FabricBlockSettings.copyOf(Blocks.BLUE_CANDLE));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AffineCandleBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (BlockEntityTicker<T>) TickedBlockEntity.ticker();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            this.getParticleOffsets(state)
                    .forEach(offset -> {
                        double x = offset.x + pos.getX();
                        double y = offset.y + pos.getY();
                        double z = offset.z + pos.getZ();

                        float f = random.nextFloat();
                        if (f < 0.3F) {
                            ClientParticles.randomizeVelocity(0.3);
                            ClientParticles.spawn(ParticleTypes.REVERSE_PORTAL, world, new Vec3d(x, y, z), 0.2);
                            if (f < 0.17F) {
                                world.playSound(
                                        x + 0.5,
                                        y + 0.5,
                                        z + 0.5,
                                        SoundEvents.BLOCK_CANDLE_AMBIENT,
                                        SoundCategory.BLOCKS,
                                        1.0F + random.nextFloat(),
                                        random.nextFloat() * 0.7F + 0.3F,
                                        false
                                );
                            }
                        }

                        // TODO: make this better™
                        world.addParticle(new SmallColoredFlameParticleEffect(DyeColor.PURPLE), x, y, z, 0.0, 0.0, 0.0);
                    });
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof AethumNetworkMemberBlockEntity member) member.onBroken();
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public Iterable<Vec3d> getParticleOffsets(BlockState state) {
        return super.getParticleOffsets(state);
    }
}
