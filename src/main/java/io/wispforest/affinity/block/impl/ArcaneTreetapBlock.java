package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.misc.util.BlockFinder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ArcaneTreetapBlock extends HorizontalFacingBlock {

    private static final VoxelShape NORTH_SHAPE = Stream.of(
            Block.createCuboidShape(1, 6, 0, 15, 10, 1),
            Block.createCuboidShape(12, 7, 1, 14, 9, 2),
            Block.createCuboidShape(2, 7, 1, 4, 9, 2),
            Block.createCuboidShape(5, 7, 1, 11, 9, 2)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape SOUTH_SHAPE = Stream.of(
            Block.createCuboidShape(1, 6, 15, 15, 10, 16),
            Block.createCuboidShape(2, 7, 14, 4, 9, 15),
            Block.createCuboidShape(12, 7, 14, 14, 9, 15),
            Block.createCuboidShape(5, 7, 14, 11, 9, 15)
    ).reduce(VoxelShapes::union).get();

    public static final VoxelShape WEST_SHAPE = Stream.of(
            Block.createCuboidShape(0, 6, 1, 1, 10, 15),
            Block.createCuboidShape(1, 7, 2, 2, 9, 4),
            Block.createCuboidShape(1, 7, 12, 2, 9, 14),
            Block.createCuboidShape(1, 7, 5, 2, 9, 11)
    ).reduce(VoxelShapes::union).get();

    public static final VoxelShape EAST_SHAPE = Stream.of(
            Block.createCuboidShape(15, 6, 1, 16, 10, 15),
            Block.createCuboidShape(14, 7, 12, 15, 9, 14),
            Block.createCuboidShape(14, 7, 2, 15, 9, 4),
            Block.createCuboidShape(14, 7, 5, 15, 9, 11)
    ).reduce(VoxelShapes::union).get();

    public ArcaneTreetapBlock() {
        super(FabricBlockSettings.copyOf(Blocks.OAK_TRAPDOOR));
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.north()).isIn(BlockTags.LOGS)
                || world.getBlockState(pos.south()).isIn(BlockTags.LOGS)
                || world.getBlockState(pos.east()).isIn(BlockTags.LOGS)
                || world.getBlockState(pos.west()).isIn(BlockTags.LOGS);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var side = ctx.getSide();
        if (side.getAxis() == Direction.Axis.Y) side = Direction.NORTH;

        return this.getDefaultState().with(FACING, side.getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            default -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
        };
    }

    @Override
    public ActionResult onUse(BlockState clickedState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        final var results = BlockFinder.findCapped(world, pos.offset(clickedState.get(FACING)), (blockPos, state) -> {
            if (state.isIn(BlockTags.LEAVES)) {
                return !state.get(LeavesBlock.PERSISTENT);
            }

            return state.isIn(BlockTags.LOGS);
        }, 128);
        final var counted = results.byCount();

        int logCount = counted.keySet().stream()
                .filter(block -> block.getRegistryEntry().isIn(BlockTags.LOGS))
                .mapToInt(counted::get).sum();
        int leavesCount = counted.keySet().stream()
                .filter(block -> block.getRegistryEntry().isIn(BlockTags.LEAVES))
                .mapToInt(counted::get).sum();

        if (logCount > 5 && leavesCount > 40) {
            player.sendMessage(Text.literal("yep, that is in fact a tree").formatted(Formatting.GREEN), false);
        } else {
            player.sendMessage(Text.literal("nope, not a tree. no.").formatted(Formatting.RED), false);
        }

        return ActionResult.SUCCESS;
    }
}
