package io.wispforest.affinity.block;

import io.wispforest.affinity.blockentity.AethumFluxCacheBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class CopperPlatedAethumFluxCacheBlock extends AethumNetworkMemberBlock {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(1, 0, 1, 15, 2, 15),
            Block.createCuboidShape(11, 2, 3, 13, 13, 5),
            Block.createCuboidShape(5, 2, 4, 11, 11, 5),
            Block.createCuboidShape(5, 2, 11, 11, 11, 12),
            Block.createCuboidShape(4, 2, 5, 5, 11, 11),
            Block.createCuboidShape(11, 2, 5, 12, 11, 11),
            Block.createCuboidShape(3, 2, 3, 5, 13, 5),
            Block.createCuboidShape(3, 2, 11, 5, 13, 13),
            Block.createCuboidShape(3, 11, 5, 13, 13, 11),
            Block.createCuboidShape(5, 11, 3, 11, 13, 5),
            Block.createCuboidShape(5, 11, 11, 11, 13, 13),
            Block.createCuboidShape(6, 13, 6, 10, 14, 10),
            Block.createCuboidShape(11, 2, 11, 13, 13, 13)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public CopperPlatedAethumFluxCacheBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque().luminance(10));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AethumFluxCacheBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
