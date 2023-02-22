package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AbstractAethumFluxNodeBlock;
import net.minecraft.block.Block;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.stream.Stream;

public class CopperPlatedAethumFluxNodeBlock extends AbstractAethumFluxNodeBlock {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(4, 0, 4, 12, 3, 12),
            Block.createCuboidShape(5, 2, 5, 11, 8, 11),
            Block.createCuboidShape(7, 2, 11, 9, 8, 12),
            Block.createCuboidShape(4, 2, 7, 5, 8, 9),
            Block.createCuboidShape(11, 2, 7, 12, 8, 9),
            Block.createCuboidShape(7, 2, 4, 9, 8, 5),
            Block.createCuboidShape(4, 8, 4, 12, 10, 12),
            Block.createCuboidShape(5, 10, 5, 11, 11, 11)
    ).reduce(VoxelShapes::union).get();

    @Override
    protected VoxelShape getShape() {
        return SHAPE;
    }

    @Override
    public boolean isUpgradeable() {
        return true;
    }

    @Override
    public float shardHeight() {
        return .675f;
    }
}
