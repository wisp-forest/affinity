package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.GravitonTransducerBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class GravitonTransducerBlock extends AethumNetworkMemberBlock {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(5, 1, 1, 11, 4, 4),
            Block.createCuboidShape(11, 0, 0, 16, 5, 5),
            Block.createCuboidShape(11, 0, 11, 16, 5, 16),
            Block.createCuboidShape(0, 0, 11, 5, 5, 16),
            Block.createCuboidShape(11, 11, 11, 16, 16, 16),
            Block.createCuboidShape(11, 11, 0, 16, 16, 5),
            Block.createCuboidShape(0, 11, 0, 5, 16, 5),
            Block.createCuboidShape(0, 11, 11, 5, 16, 16),
            Block.createCuboidShape(0, 0, 0, 5, 5, 5),
            Block.createCuboidShape(5, 1, 12, 11, 4, 15),
            Block.createCuboidShape(5, 12, 12, 11, 15, 15),
            Block.createCuboidShape(5, 12, 1, 11, 15, 4),
            Block.createCuboidShape(1, 12, 5, 4, 15, 11),
            Block.createCuboidShape(12, 12, 5, 15, 15, 11),
            Block.createCuboidShape(1, 1, 5, 4, 4, 11),
            Block.createCuboidShape(12, 1, 5, 15, 4, 11),
            Block.createCuboidShape(1, 5, 1, 4, 11, 4),
            Block.createCuboidShape(12, 5, 1, 15, 11, 4),
            Block.createCuboidShape(1, 5, 12, 4, 11, 15),
            Block.createCuboidShape(12, 5, 12, 15, 11, 15)
    ).reduce(VoxelShapes::union).get();

    public GravitonTransducerBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.GRAVITON_TRANSDUCER, TickedBlockEntity.ticker());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GravitonTransducerBlockEntity(pos, state);
    }
}
