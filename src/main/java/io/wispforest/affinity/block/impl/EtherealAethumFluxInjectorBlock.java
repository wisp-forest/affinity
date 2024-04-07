package io.wispforest.affinity.block.impl;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.blockentity.impl.EtherealAethumFluxInjectorBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class EtherealAethumFluxInjectorBlock extends BlockWithEntity {

    private static final VoxelShape DOWN_SHAPE = Stream.of(
            Block.createCuboidShape(2, 0, 2, 14, 3, 5),
            Block.createCuboidShape(2, 0, 5, 5, 3, 11),
            Block.createCuboidShape(11, 0, 5, 14, 3, 11),
            Block.createCuboidShape(2, 0, 11, 14, 3, 14)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape UP_SHAPE = Stream.of(
            Block.createCuboidShape(2, 13, 2, 14, 16, 5),
            Block.createCuboidShape(11, 13, 5, 14, 16, 11),
            Block.createCuboidShape(2, 13, 5, 5, 16, 11),
            Block.createCuboidShape(2, 13, 11, 14, 16, 14)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape NORTH_SHAPE = Stream.of(
            Block.createCuboidShape(2, 2, 0, 14, 5, 3),
            Block.createCuboidShape(11, 5, 0, 14, 11, 3),
            Block.createCuboidShape(2, 5, 0, 5, 11, 3),
            Block.createCuboidShape(2, 11, 0, 14, 14, 3)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape SOUTH_SHAPE = Stream.of(
            Block.createCuboidShape(2, 2, 13, 14, 5, 16),
            Block.createCuboidShape(2, 5, 13, 5, 11, 16),
            Block.createCuboidShape(11, 5, 13, 14, 11, 16),
            Block.createCuboidShape(2, 11, 13, 14, 14, 16)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape WEST_SHAPE = Stream.of(
            Block.createCuboidShape(0, 2, 2, 3, 5, 14),
            Block.createCuboidShape(0, 5, 2, 3, 11, 5),
            Block.createCuboidShape(0, 5, 11, 3, 11, 14),
            Block.createCuboidShape(0, 11, 2, 3, 14, 14)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape EAST_SHAPE = Stream.of(
            Block.createCuboidShape(13, 2, 2, 16, 5, 14),
            Block.createCuboidShape(13, 5, 11, 16, 11, 14),
            Block.createCuboidShape(13, 5, 2, 16, 11, 5),
            Block.createCuboidShape(13, 11, 2, 16, 14, 14)
    ).reduce(VoxelShapes::union).get();

    public static final EnumProperty<Direction> FACING = Properties.FACING;

    public EtherealAethumFluxInjectorBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque().luminance(10));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case UP -> UP_SHAPE;
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
        };
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return !world.getBlockState(pos.offset(state.get(FACING))).isAir();
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction != state.get(FACING)) return state;
        return !world.getBlockState(neighborPos).isAir() ? state : Blocks.AIR.getDefaultState();
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getSide().getOpposite());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof EtherealAethumFluxInjectorBlockEntity injector && injector.lastKnownSourceNode() != null) {
                world.getScoreboard().getComponent(AffinityComponents.ETHEREAL_NODE_STORAGE).removeInjector(injector.lastKnownSourceNode(), GlobalPos.create(world.getRegistryKey(), injector.getPos()));
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EtherealAethumFluxInjectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? validateTicker(type, AffinityBlocks.Entities.ETHEREAL_AETHUM_FLUX_INJECTOR, TickedBlockEntity.ticker()) : null;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }
}
