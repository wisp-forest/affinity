package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.AffineInfuserBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class AffineInfuserBlock extends AethumNetworkMemberBlock {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(4, 0, 4, 12, 2, 12),
            Block.createCuboidShape(4, 14, 4, 12, 16, 12),
            Block.createCuboidShape(5, 2, 5, 11, 14, 11)
    ).reduce(VoxelShapes::union).get();

    public AffineInfuserBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS), CONSUMER_TOOLTIP);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.AFFINE_INFUSER, TickedBlockEntity.ticker());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AffineInfuserBlockEntity(pos, state);
    }
}
