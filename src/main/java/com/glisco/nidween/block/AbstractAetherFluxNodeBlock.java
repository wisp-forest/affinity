package com.glisco.nidween.block;

import com.glisco.owo.ItemOps;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAetherFluxNodeBlock extends BlockWithEntity {

    public static BooleanProperty SHARD = BooleanProperty.of("shard");

    protected AbstractAetherFluxNodeBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque().luminance(10));
        setDefaultState(getStateManager().getDefaultState().with(SHARD, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHARD);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AetherFluxNodeBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        final var playerStack = player.getStackInHand(hand);

        if (state.get(SHARD)) {
            if (ItemOps.canStack(playerStack, new ItemStack(Items.AMETHYST_SHARD))) {
                playerStack.increment(1);
                world.setBlockState(pos, state.with(SHARD, false));
                return ActionResult.SUCCESS;
            } else if (playerStack.isEmpty()) {
                player.setStackInHand(hand, new ItemStack(Items.AMETHYST_SHARD));
                world.setBlockState(pos, state.with(SHARD, false));
                return ActionResult.SUCCESS;
            }
        } else if (playerStack.isOf(Items.AMETHYST_SHARD)) {
            if (!ItemOps.emptyAwareDecrement(playerStack)) player.setStackInHand(hand, ItemStack.EMPTY);
            world.setBlockState(pos, state.with(SHARD, true));
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(SHARD) ? getShapeWithShard() : getEmptyShape();
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if(state.getBlock() != newState.getBlock()){
            if(state.get(SHARD)) {
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.AMETHYST_SHARD));
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    protected abstract VoxelShape getEmptyShape();

    protected abstract VoxelShape getShapeWithShard();
}
