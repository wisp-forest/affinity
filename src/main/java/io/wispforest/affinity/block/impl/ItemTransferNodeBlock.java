package io.wispforest.affinity.block.impl;

import com.google.common.collect.ImmutableMap;
import io.wispforest.affinity.block.template.ScrollInteractionReceiver;
import io.wispforest.affinity.blockentity.impl.ItemTransferNodeBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ItemTransferNodeBlock extends BlockWithEntity implements ScrollInteractionReceiver {

    public static final DirectionProperty FACING = Properties.FACING;

    private static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.of(
            Direction.DOWN, VoxelShapes.union(Block.createCuboidShape(5, 0, 5, 11, 4, 11), Block.createCuboidShape(4, 1, 4, 12, 3, 12)),
            Direction.UP, VoxelShapes.union(Block.createCuboidShape(5, 12, 5, 11, 16, 11), Block.createCuboidShape(4, 13, 4, 12, 15, 12)),
            Direction.NORTH, VoxelShapes.union(Block.createCuboidShape(5, 5, 0, 11, 11, 4), Block.createCuboidShape(4, 4, 1, 12, 12, 3)),
            Direction.SOUTH, VoxelShapes.union(Block.createCuboidShape(5, 5, 12, 11, 11, 16), Block.createCuboidShape(4, 4, 13, 12, 12, 15)),
            Direction.WEST, VoxelShapes.union(Block.createCuboidShape(0, 5, 5, 4, 11, 11), Block.createCuboidShape(1, 4, 4, 3, 12, 12)),
            Direction.EAST, VoxelShapes.union(Block.createCuboidShape(12, 5, 5, 16, 11, 11), Block.createCuboidShape(13, 4, 4, 15, 12, 12))
    );

    public ItemTransferNodeBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE));
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.DOWN));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getSide().getOpposite());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, AffinityBlocks.Entities.ITEM_TRANSFER_NODE, TickedBlockEntity.ticker());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ItemTransferNodeBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
    }

    @Override
    public @NotNull ActionResult onScroll(World world, BlockState state, BlockPos pos, PlayerEntity player, boolean direction) {
        if (world.getBlockEntity(pos) instanceof ItemTransferNodeBlockEntity node) {
            return node.onScroll(player, direction);
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof ItemTransferNodeBlockEntity node) node.onBroken();
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}
