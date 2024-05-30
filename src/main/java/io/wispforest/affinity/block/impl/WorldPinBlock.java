package io.wispforest.affinity.block.impl;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.blockentity.impl.WorldPinBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.particle.BezierPathEmitterParticleEffect;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.util.VectorRandomUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class WorldPinBlock extends BlockWithEntity {

    public static final BooleanProperty ENABLED = Properties.ENABLED;
    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(11, 0, 7, 12, 5, 9),
            Block.createCuboidShape(4, 5, 4, 12, 7, 12),
            Block.createCuboidShape(7, 0, 11, 9, 5, 12),
            Block.createCuboidShape(7, 0, 4, 9, 5, 5),
            Block.createCuboidShape(4, 0, 7, 5, 5, 9),
            Block.createCuboidShape(4, 0, 7, 5, 5, 9),
            Block.createCuboidShape(5, 0, 5, 11, 8, 11)
    ).reduce(VoxelShapes::union).get();

    public WorldPinBlock() {
        super(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_TILES).mapColor(MapColor.CYAN).sounds(BlockSoundGroup.METAL).luminance(10).solid());
        this.setDefaultState(this.getDefaultState().with(ENABLED, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!state.get(ENABLED)) return;

        ClientParticles.spawn(
                new BezierPathEmitterParticleEffect(
                        new DustParticleEffect(MathUtil.rgbToVec3f(0x00aeb4), .5f),
                        VectorRandomUtils.getRandomOffsetSpecific(world, Vec3d.ofCenter(pos, 5), 3, 1, 3),
                        25, 10, true
                ), world, Vec3d.ofCenter(pos, .85), .3
        );
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof WorldPinBlockEntity pin) pin.onBroken();
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, AffinityBlocks.Entities.WORLD_PIN, TickedBlockEntity.ticker());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WorldPinBlockEntity(pos, state);
    }
}

