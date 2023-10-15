package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.block.template.ScrollInteractionReceiver;
import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.item.StaffItem;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class StaffPedestalBlock extends AethumNetworkMemberBlock implements ScrollInteractionReceiver {

    private static final VoxelShape SHAPE = Stream.of(
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

    public StaffPedestalBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
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
