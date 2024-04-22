package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.CropReaperBlockEntity;
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

public class CropReaperBlock extends AethumNetworkMemberBlock {

    public static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(9, 16, 6, 11, 17, 10),
            Block.createCuboidShape(3, 3, 3, 13, 16, 13),
            Block.createCuboidShape(2, 10, 2, 14, 13, 14),
            Block.createCuboidShape(2, 6, 2, 14, 9, 14),
            Block.createCuboidShape(1, 0, 1, 15, 3, 15),
            Block.createCuboidShape(5, 16, 6, 7, 17, 10)
    ).reduce(VoxelShapes::union).get();

    public CropReaperBlock() {
        super(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque(), GENERATOR_TOOLTIP);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.CROP_REAPER, TickedBlockEntity.ticker());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CropReaperBlockEntity(pos, state);
    }
}
