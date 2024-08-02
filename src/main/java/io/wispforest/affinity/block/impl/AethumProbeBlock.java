package io.wispforest.affinity.block.impl;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.blockentity.impl.AethumProbeBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
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
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class AethumProbeBlock extends BlockWithEntity {

    public static final IntProperty CRYSTALS = IntProperty.of("crystals", 0, 4);
    public static final BooleanProperty POWERED = Properties.POWERED;

    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    private static final Direction[] UPDATE_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN};

    public AethumProbeBlock() {
        super(FabricBlockSettings.copyOf(Blocks.REPEATER));
        this.setDefaultState(this.getDefaultState().with(CRYSTALS, 0).with(POWERED, false));
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack playerStack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int crystals = state.get(CRYSTALS);
        if (!playerStack.isOf(Items.AMETHYST_SHARD) || crystals >= 4) return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;

        crystals++;
        world.setBlockState(pos, state.with(CRYSTALS, crystals));

        if (!ItemOps.emptyAwareDecrement(playerStack)) player.setStackInHand(hand, ItemStack.EMPTY);
        return ItemActionResult.SUCCESS;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        int crystals = state.get(CRYSTALS);
        if (crystals == 0) return ActionResult.PASS;

        crystals--;
        world.setBlockState(pos, state.with(CRYSTALS, crystals));

        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), Items.AMETHYST_SHARD.getDefaultStack());
        return ActionResult.SUCCESS;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return direction != Direction.DOWN && state.get(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return this.getWeakRedstonePower(state, world, pos, direction);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!this.canPlaceAt(state, world, pos)) {
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            if (state.get(POWERED)) {
                updateNeighbors(world, pos);
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    public static void updateNeighbors(World world, BlockPos pos) {
        for (var direction : UPDATE_DIRECTIONS) {
            world.updateNeighborsAlways(pos.offset(direction), AffinityBlocks.AETHUM_PROBE);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AethumProbeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, AffinityBlocks.Entities.AETHUM_PROBE, !world.isClient ? TickedBlockEntity.ticker() : null);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CRYSTALS).add(POWERED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return hasTopRim(world, pos.down());
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }
}
