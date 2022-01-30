package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.ArborealAccumulationApparatusBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ArborealAccumulationApparatusBlock extends AethumNetworkMemberBlock {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(4, 0, 9, 7, 12, 12),
            Block.createCuboidShape(9, 0, 4, 12, 12, 7),
            Block.createCuboidShape(5, 0, 7, 7, 10, 9),
            Block.createCuboidShape(9, 0, 7, 11, 10, 9),
            Block.createCuboidShape(7, 0, 5, 9, 10, 11),
            Block.createCuboidShape(4, 0, 4, 7, 12, 7),
            Block.createCuboidShape(9, 0, 9, 12, 12, 12)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public ArborealAccumulationApparatusBlock() {
        super(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.ARBOREAL_ACCUMULATION_APPARATUS, TickedBlockEntity.ticker());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ArborealAccumulationApparatusBlockEntity(pos, state);
    }
}
