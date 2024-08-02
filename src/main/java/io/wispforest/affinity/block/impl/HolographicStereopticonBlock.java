package io.wispforest.affinity.block.impl;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.block.template.BlockItemProvider;
import io.wispforest.affinity.block.template.ScrollInteractionReceiver;
import io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.item.HolographicStereopticonBlockItem;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class HolographicStereopticonBlock extends BlockWithEntity implements BlockItemProvider, ScrollInteractionReceiver {

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(3, 8, 11, 5, 10, 13),
            Block.createCuboidShape(0, 6, 0, 16, 8, 16),
            Block.createCuboidShape(1, 2, 1, 15, 9, 15),
            Block.createCuboidShape(11, 8, 11, 13, 10, 13),
            Block.createCuboidShape(3, 8, 3, 5, 10, 5),
            Block.createCuboidShape(11, 8, 3, 13, 10, 5),
            Block.createCuboidShape(6, 9, 6, 10, 10, 10),
            Block.createCuboidShape(0, 0, 0, 16, 2, 16)
    ).reduce(VoxelShapes::union).get();

    public HolographicStereopticonBlock() {
        super(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_TILES).nonOpaque());
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HolographicStereopticonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? validateTicker(type, AffinityBlocks.Entities.HOLOGRAPHIC_STEREOPTICON, TickedBlockEntity.ticker()) : null;
    }

    @Override
    public Item createBlockItem(Block block, OwoItemSettings settings) {
        return new HolographicStereopticonBlockItem(block, settings);
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
    public @NotNull ActionResult onScroll(World world, BlockState state, BlockPos pos, PlayerEntity player, boolean direction) {
        if (!(world.getBlockEntity(pos) instanceof HolographicStereopticonBlockEntity stereopticon)) {
            return ActionResult.PASS;
        }

        stereopticon.changeScale(direction);
        return ActionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }
}
