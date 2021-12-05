package io.wispforest.affinity.block;

import io.wispforest.affinity.blockentity.SundialBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class SundialBlock extends AetherNetworkMemberBlock {

    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 2, 14);

    public SundialBlock() {
        super(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).nonOpaque());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SundialBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
