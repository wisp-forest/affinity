package io.wispforest.affinity.block.template;

import io.wispforest.affinity.blockentity.impl.AethumFluxNodeBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
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

public abstract class AbstractAethumFluxNodeBlock extends AethumNetworkMemberBlock {

    public static BooleanProperty SHARD = BooleanProperty.of("shard");

    protected AbstractAethumFluxNodeBlock() {
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
        return new AethumFluxNodeBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (player.isSneaking() && world.getBlockEntity(pos) instanceof AethumFluxNodeBlockEntity node) {
            node.onUse(player, hand, hit);
            return ActionResult.SUCCESS;
        }

        final var playerStack = player.getStackInHand(hand);

        if (state.get(SHARD)) {
            if (playerStack.isEmpty()) {
                player.setStackInHand(hand, new ItemStack(Items.AMETHYST_SHARD));
                world.setBlockState(pos, state.with(SHARD, false));
                return ActionResult.SUCCESS;
            } else if (ItemOps.canStack(playerStack, new ItemStack(Items.AMETHYST_SHARD))) {
                playerStack.increment(1);
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.AETHUM_FLUX_NODE, TickedBlockEntity.ticker());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (state.get(SHARD)) {
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.AMETHYST_SHARD));
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    protected abstract VoxelShape getEmptyShape();

    protected abstract VoxelShape getShapeWithShard();
}
