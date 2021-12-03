package com.glisco.affinity.block;

import net.minecraft.block.Block;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.stream.Stream;

public class StoneBandedAetherFluxNodeBlock extends AbstractAetherFluxNodeBlock{

    private static final VoxelShape EMPTY_SHAPE;
    private static final VoxelShape SHAPE_WITH_SHARD;

    static {
        EMPTY_SHAPE = Stream.of(
                Block.createCuboidShape(2, 0, 2, 4, 5, 14),
                Block.createCuboidShape(4, 0, 2, 12, 5, 4),
                Block.createCuboidShape(4, 0, 12, 12, 5, 14),
                Block.createCuboidShape(6, 0, 14, 10, 5, 16),
                Block.createCuboidShape(6, 0, 0, 10, 5, 2),
                Block.createCuboidShape(14, 0, 6, 16, 5, 10),
                Block.createCuboidShape(0, 0, 6, 2, 5, 10),
                Block.createCuboidShape(4, 3, 6, 5, 6, 10),
                Block.createCuboidShape(11, 3, 6, 12, 6, 10),
                Block.createCuboidShape(6, 3, 11, 10, 6, 12),
                Block.createCuboidShape(6, 3, 4, 10, 6, 5),
                Block.createCuboidShape(6, 5, 12, 10, 6, 16),
                Block.createCuboidShape(12, 5, 6, 16, 6, 10),
                Block.createCuboidShape(0, 5, 6, 4, 6, 10),
                Block.createCuboidShape(6, 5, 0, 10, 6, 4),
                Block.createCuboidShape(4, 0, 4, 12, 3, 12),
                Block.createCuboidShape(4, 2, 4, 12, 3, 12),
                Block.createCuboidShape(12, 0, 2, 14, 5, 14)
        ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

        SHAPE_WITH_SHARD = VoxelShapes.combineAndSimplify(EMPTY_SHAPE, Block.createCuboidShape(7, 3, 7, 9, 9, 9), BooleanBiFunction.OR);
    }

    @Override
    protected VoxelShape getEmptyShape() {
        return EMPTY_SHAPE;
    }

    @Override
    protected VoxelShape getShapeWithShard() {
        return SHAPE_WITH_SHARD;
    }
}
