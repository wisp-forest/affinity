package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.block.template.ScrollInteractionReceiver;
import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.item.StaffItem;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class StaffPedestalBlock extends AethumNetworkMemberBlock implements ScrollInteractionReceiver, Waterloggable {

    private static final VoxelShape UP_SHAPE = Stream.of(
            Block.createCuboidShape(2, 9, 2, 4, 16, 4),
            Block.createCuboidShape(2, 0, 2, 14, 2, 14),
            Block.createCuboidShape(4, 2, 4, 12, 15, 12),
            Block.createCuboidShape(4, 10, 12, 12, 14, 13),
            Block.createCuboidShape(4, 10, 3, 12, 14, 4),
            Block.createCuboidShape(12, 10, 4, 13, 14, 12),
            Block.createCuboidShape(3, 10, 4, 4, 14, 12),
            Block.createCuboidShape(12, 9, 2, 14, 16, 4),
            Block.createCuboidShape(12, 9, 12, 14, 16, 14),
            Block.createCuboidShape(2, 9, 12, 4, 16, 14)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape DOWN_SHAPE = Stream.of(
            Block.createCuboidShape(2, 14, 2, 14, 16, 14),
            Block.createCuboidShape(4, 1, 4, 12, 14, 12),
            Block.createCuboidShape(4, 2, 12, 12, 6, 13),
            Block.createCuboidShape(4, 2, 3, 12, 6, 4),
            Block.createCuboidShape(3, 2, 4, 4, 6, 12),
            Block.createCuboidShape(12, 2, 4, 13, 6, 12),
            Block.createCuboidShape(2, 0, 12, 4, 7, 14),
            Block.createCuboidShape(2, 0, 2, 4, 7, 4),
            Block.createCuboidShape(12, 0, 2, 14, 7, 4),
            Block.createCuboidShape(12, 0, 12, 14, 7, 14)
    ).reduce(VoxelShapes::union).get();

    public static final EnumProperty<Direction> FACING = Properties.VERTICAL_DIRECTION;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public StaffPedestalBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque(), CONSUMER_TOOLTIP);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.UP).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getSide() == Direction.DOWN ? Direction.DOWN : Direction.UP)
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return this.canPlaceAt(state, world, pos)
                ? state
                : Blocks.AIR.getDefaultState();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(FACING) == Direction.UP ? UP_SHAPE : DOWN_SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, Hand.MAIN_HAND, hit);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandleWithItem(world, pos, player, hand, hit);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, AffinityBlocks.Entities.STAFF_PEDESTAL, TickedBlockEntity.ticker());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StaffPedestalBlockEntity(pos, state);
    }

    @Override
    public @NotNull ActionResult onScroll(World world, BlockState state, BlockPos pos, PlayerEntity player, boolean direction) {
        if (world.getBlockEntity(pos) instanceof StaffPedestalBlockEntity pedestal && pedestal.getItem().getItem() instanceof StaffItem staff) {
            return staff.onPedestalScrolled(world, pos, pedestal, direction);
        } else {
            return ActionResult.PASS;
        }
    }
}
