package io.wispforest.affinity.block.impl;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.blockentity.impl.SonicSyphonBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SonicSyphonBlock extends HorizontalFacingBlock implements BlockEntityProvider {

    public static final VoxelShape NORTH_SHAPE = VoxelShapes.union(
        Block.createCuboidShape(0, 0, 8, 16, 16, 16),
        Block.createCuboidShape(3, 3, 1, 13, 13, 8)
    );
    public static final VoxelShape SOUTH_SHAPE = VoxelShapes.union(
        Block.createCuboidShape(0, 0, 0, 16, 16, 8),
        Block.createCuboidShape(3, 3, 8, 13, 13, 15)
    );
    public static final VoxelShape EAST_SHAPE = VoxelShapes.union(
        Block.createCuboidShape(0, 0, 0, 8, 16, 16),
        Block.createCuboidShape(8, 3, 3, 15, 13, 13)
    );
    public static final VoxelShape WEST_SHAPE = VoxelShapes.union(
        Block.createCuboidShape(8, 0, 0, 16, 16, 16),
        Block.createCuboidShape(1, 3, 3, 8, 13, 13)
    );

    public SonicSyphonBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        var side = ctx.getSide();
        if (side.getAxis() == Direction.Axis.Y) {
            side = ctx.getHorizontalPlayerFacing().getOpposite();
        }

        return this.getDefaultState().with(FACING, side);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SonicSyphonBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return !world.isClient && type == AffinityBlocks.Entities.SONIC_SYPHON ? (BlockEntityTicker<T>) TickedBlockEntity.ticker() : null;
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return null;
    }
}
