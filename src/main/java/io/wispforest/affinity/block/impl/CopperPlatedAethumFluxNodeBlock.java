package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AbstractAethumFluxNodeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class CopperPlatedAethumFluxNodeBlock extends AbstractAethumFluxNodeBlock {

    public static final Property<Direction> FACING = Properties.FACING;

    private static final VoxelShape UP_SHAPE = Stream.of(
            Block.createCuboidShape(4, 0, 4, 12, 3, 12),
            Block.createCuboidShape(5, 2, 5, 11, 8, 11),
            Block.createCuboidShape(7, 2, 11, 9, 8, 12),
            Block.createCuboidShape(4, 2, 7, 5, 8, 9),
            Block.createCuboidShape(11, 2, 7, 12, 8, 9),
            Block.createCuboidShape(7, 2, 4, 9, 8, 5),
            Block.createCuboidShape(4, 8, 4, 12, 10, 12),
            Block.createCuboidShape(5, 10, 5, 11, 11, 11)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape DOWN_SHAPE = Stream.of(
            Block.createCuboidShape(4, 13, 4, 12, 16, 12),
            Block.createCuboidShape(4, 6, 4, 12, 8, 12),
            Block.createCuboidShape(5, 5, 5, 11, 13, 11),
            Block.createCuboidShape(7, 8, 4, 9, 13, 5),
            Block.createCuboidShape(11, 8, 7, 12, 13, 9),
            Block.createCuboidShape(4, 8, 7, 5, 13, 9),
            Block.createCuboidShape(7, 8, 11, 9, 13, 12)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape SOUTH_SHAPE = Stream.of(
            Block.createCuboidShape(4, 4, 0, 12, 12, 3),
            Block.createCuboidShape(4, 4, 8, 12, 12, 10),
            Block.createCuboidShape(5, 5, 3, 11, 11, 11),
            Block.createCuboidShape(7, 4, 3, 9, 5, 8),
            Block.createCuboidShape(11, 7, 3, 12, 9, 8),
            Block.createCuboidShape(4, 7, 3, 5, 9, 8),
            Block.createCuboidShape(7, 11, 3, 9, 12, 8)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape NORTH_SHAPE = Stream.of(
            Block.createCuboidShape(4, 4, 13, 12, 12, 16),
            Block.createCuboidShape(4, 4, 6, 12, 12, 8),
            Block.createCuboidShape(5, 5, 5, 11, 11, 13),
            Block.createCuboidShape(7, 4, 8, 9, 5, 13),
            Block.createCuboidShape(4, 7, 8, 5, 9, 13),
            Block.createCuboidShape(11, 7, 8, 12, 9, 13),
            Block.createCuboidShape(7, 11, 8, 9, 12, 13)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape EAST_SHAPE = Stream.of(
            Block.createCuboidShape(0, 4, 4, 3, 12, 12),
            Block.createCuboidShape(8, 4, 4, 10, 12, 12),
            Block.createCuboidShape(3, 5, 5, 11, 11, 11),
            Block.createCuboidShape(3, 4, 7, 8, 5, 9),
            Block.createCuboidShape(3, 7, 4, 8, 9, 5),
            Block.createCuboidShape(3, 7, 11, 8, 9, 12),
            Block.createCuboidShape(3, 11, 7, 8, 12, 9)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape WEST_SHAPE = Stream.of(
            Block.createCuboidShape(13, 4, 4, 16, 12, 12),
            Block.createCuboidShape(6, 4, 4, 8, 12, 12),
            Block.createCuboidShape(5, 5, 5, 13, 11, 11),
            Block.createCuboidShape(8, 4, 7, 13, 5, 9),
            Block.createCuboidShape(8, 7, 11, 13, 9, 12),
            Block.createCuboidShape(8, 7, 4, 13, 9, 5),
            Block.createCuboidShape(8, 11, 7, 13, 12, 9)
    ).reduce(VoxelShapes::union).get();

    public CopperPlatedAethumFluxNodeBlock() {
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case UP -> UP_SHAPE;
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(FACING, ctx.getSide());
    }

    @Override
    public boolean isUpgradeable() {
        return true;
    }

    @Override
    public float shardHeight() {
        return .8f;
    }
}
