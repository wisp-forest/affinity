package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.BlockItemProvider;
import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import io.wispforest.affinity.item.DirectInteractionHandler;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public class AssemblyAugmentBlock extends BlockWithEntity implements BlockItemProvider {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(-1, 0, 3, 3, 3, 13),
            Block.createCuboidShape(-1, 0, -1, 17, 3, 3),
            Block.createCuboidShape(4, 2, -2, 12, 4, 0),
            Block.createCuboidShape(4, 2, 16, 12, 4, 18),
            Block.createCuboidShape(16, 2, 4, 18, 4, 12),
            Block.createCuboidShape(-2, 2, 4, 0, 4, 12),
            Block.createCuboidShape(13, -3, -1, 17, 0, 3),
            Block.createCuboidShape(13, -3, 13, 17, 0, 17),
            Block.createCuboidShape(-1, -3, 13, 3, 0, 17),
            Block.createCuboidShape(-1, -3, -1, 3, 0, 3),
            Block.createCuboidShape(-1, 0, 13, 17, 3, 17),
            Block.createCuboidShape(13, 0, 3, 17, 3, 13)
    ).reduce(VoxelShapes::union).get();

    public AssemblyAugmentBlock() {
        super(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque());
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isOf(Blocks.CRAFTING_TABLE);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction != Direction.DOWN) return state;
        return world.getBlockState(neighborPos).isOf(Blocks.CRAFTING_TABLE) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AssemblyAugmentBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public Item createBlockItem(Block block, OwoItemSettings settings) {
        return new AssemblyAugmentItem(block, settings);
    }

    private static class AssemblyAugmentItem extends BlockItem implements DirectInteractionHandler {

        public AssemblyAugmentItem(Block block, Settings settings) {
            super(block, settings);
        }

        @Override
        public Collection<Block> interactionOverrideCandidates() {
            return Set.of(Blocks.CRAFTING_TABLE);
        }
    }
}
