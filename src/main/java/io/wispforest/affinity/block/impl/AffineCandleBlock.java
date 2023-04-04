package io.wispforest.affinity.block.impl;

import com.google.common.collect.ImmutableList;
import io.wispforest.affinity.blockentity.impl.AffineCandleBlockEntity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.particle.SmallColoredFlameParticleEffect;
import io.wispforest.owo.particles.ClientParticles;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AffineCandleBlock extends CandleBlock implements BlockEntityProvider {

    public static final Int2ObjectMap<List<Vec3d>> CANDLES_TO_PARTICLE_OFFSETS = Util.make(
            () -> {
                var offsets = new Int2ObjectOpenHashMap<List<Vec3d>>();

                offsets.put(1, ImmutableList.of(new Vec3d(0.5, 0.5, 0.5)));
                offsets.put(2, ImmutableList.of(new Vec3d(0.375, 0.44, 0.5), new Vec3d(0.625, 0.5, 0.44)));
                offsets.put(3, ImmutableList.of(new Vec3d(0.625, 0.313, 0.4375), new Vec3d(0.4375, 0.44, 0.4375), new Vec3d(0.5, 0.5, 0.625)));
                offsets.put(
                        4, ImmutableList.of(new Vec3d(0.625, 0.313, 0.4375), new Vec3d(0.4375, 0.44, 0.4375), new Vec3d(0.3125, 0.44, 0.625), new Vec3d(0.5, 0.5, 0.625))
                );

                return Int2ObjectMaps.unmodifiable(offsets);
            }
    );

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
        if (!state.get(LIT)) return;

        this.getParticleOffsets(state).forEach(offset -> {
            double x = pos.getX() + offset.x;
            double y = pos.getY() + offset.y;
            double z = pos.getZ() + offset.z;

            world.addParticle(new SmallColoredFlameParticleEffect(DyeColor.PURPLE), x, y, z, 0.0, 0.0, 0.0);

            float chance = random.nextFloat();
            if (chance < .3f && world.getBlockEntity(pos) instanceof AffineCandleBlockEntity candle && candle.flux() >= 100) {
                ClientParticles.randomizeVelocity(0.3);
                ClientParticles.spawn(ParticleTypes.REVERSE_PORTAL, world, new Vec3d(x, y, z), 0.2);

                if (chance > .15f) return;
                world.playSound(
                        x + .5, y + .5, z + .5,
                        SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS,
                        1f + random.nextFloat(),
                        random.nextFloat() * .7f + .3f,
                        false
                );
            }
        });
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
        return CANDLES_TO_PARTICLE_OFFSETS.get((int) state.get(CANDLES));
    }
}
