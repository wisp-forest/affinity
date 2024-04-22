package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.MatterHarvestingHearthBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class MatterHarvestingHearthBlock extends AethumNetworkMemberBlock {

    public static final BooleanProperty LIT = Properties.LIT;
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;

    private static final VoxelShape X_SHAPE = Stream.of(
            Block.createCuboidShape(0, 0, 0, 16, 4, 4),
            Block.createCuboidShape(6, 4, 1, 10, 5, 3),
            Block.createCuboidShape(6, 4, 13, 10, 5, 15),
            Block.createCuboidShape(0, 0, 12, 16, 4, 16),
            Block.createCuboidShape(1, 0, 4, 4, 4, 12),
            Block.createCuboidShape(12, 0, 4, 15, 4, 12),
            Block.createCuboidShape(4, 1, 4, 12, 3, 12)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape Z_SHAPE = Stream.of(
            Block.createCuboidShape(0, 0, 0, 4, 4, 16),
            Block.createCuboidShape(1, 4, 6, 3, 5, 10),
            Block.createCuboidShape(13, 4, 6, 15, 5, 10),
            Block.createCuboidShape(12, 0, 0, 16, 4, 16),
            Block.createCuboidShape(4, 0, 12, 12, 4, 15),
            Block.createCuboidShape(4, 0, 1, 12, 4, 4),
            Block.createCuboidShape(4, 1, 4, 12, 3, 12)
    ).reduce(VoxelShapes::union).get();

    public MatterHarvestingHearthBlock() {
        super(FabricBlockSettings.copyOf(Blocks.CAMPFIRE), GENERATOR_TOOLTIP);
        this.setDefaultState(this.getDefaultState().with(LIT, false).with(AXIS, Direction.Axis.X));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT, AXIS);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(AXIS, ctx.getHorizontalPlayerFacing().getAxis());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(AXIS) == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.MATTER_HARVESTING_HEARTH, TickedBlockEntity.ticker());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!(world.getBlockEntity(pos) instanceof MatterHarvestingHearthBlockEntity hearth)) return;
        hearth.randomDisplayTick(random);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MatterHarvestingHearthBlockEntity(pos, state);
    }
}
