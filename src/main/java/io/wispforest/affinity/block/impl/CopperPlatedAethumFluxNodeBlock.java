package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AbstractAethumFluxNodeBlock;
import net.minecraft.block.Block;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.stream.Stream;

public class CopperPlatedAethumFluxNodeBlock extends AbstractAethumFluxNodeBlock {

    private static final VoxelShape EMPTY_SHAPE;
    private static final VoxelShape SHAPE_WITH_SHARD;

    static {
        EMPTY_SHAPE = Stream.of(
                Block.createCuboidShape(5, 0, 5, 11, 2, 11),
                Block.createCuboidShape(6, 2, 6, 10, 8, 10),
                Block.createCuboidShape(5, 8, 5, 11, 10, 11)
        ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

        SHAPE_WITH_SHARD = VoxelShapes.combineAndSimplify(EMPTY_SHAPE, createCuboidShape(7, 11, 7, 9, 17, 9), BooleanBiFunction.OR);
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
