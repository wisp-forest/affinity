package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.VillagerArmatureBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SkullBlock;
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
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class VillagerArmatureBlock extends AethumNetworkMemberBlock {

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = VoxelShapes.union(
        Block.createCuboidShape(4, 2, 4, 12, 16, 12),
        Block.createCuboidShape(2, 0, 2, 14, 2, 14)
    );

    private static final VoxelShape SIDES_SHAPE = VoxelShapes.union(
        SHAPE,
        Block.createCuboidShape(0, 15, 0, 16, 16, 16)
    );

    public VillagerArmatureBlock(Settings settings) {
        super(settings, CONSUMER_TOOLTIP);

        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (newState.getBlock() != state.getBlock()) {
            if (!(world.getBlockEntity(pos) instanceof VillagerArmatureBlockEntity armature) || armature.heldStack().isEmpty()) return;
            ItemScatterer.spawn(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, armature.heldStack());

            super.onStateReplaced(state, world, pos, newState, moved);
        } else {
            super.onStateReplaced(state, world, pos, newState, moved);

            var newFacing = newState.get(FACING);
            if (newFacing != state.get(FACING)) {
                var stateAbove = world.getBlockState(pos.up());
                if (!(stateAbove.getBlock() instanceof SkullBlock)) return;

                world.setBlockState(pos.up(), stateAbove.with(
                    SkullBlock.ROTATION,
                    switch (newFacing) {
                        case NORTH -> 0;
                        case EAST -> 4;
                        case SOUTH -> 8;
                        default -> 12;
                    }
                ));
            }
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

        if (world.isReceivingRedstonePower(pos) && world.getBlockEntity(pos) instanceof VillagerArmatureBlockEntity armature) {
            armature.redstoneTriggered();
        }
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(FACING, ctx.shouldCancelInteraction() ? ctx.getHorizontalPlayerFacing() : ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return SIDES_SHAPE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VillagerArmatureBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, Hand.MAIN_HAND, hit);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, AffinityBlocks.Entities.VILLAGER_ARMATURE, TickedBlockEntity.ticker());
    }
}
