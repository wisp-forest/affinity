package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.RitualCoreBlock;
import io.wispforest.affinity.blockentity.impl.AspRiteCoreBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
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
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class AspRiteCoreBlock extends RitualCoreBlock {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(5, 9, 11, 11, 13, 14),
            Block.createCuboidShape(0, 0, 0, 16, 9, 16),
            Block.createCuboidShape(2, 9, 2, 5, 13, 14),
            Block.createCuboidShape(5, 9, 5, 11, 10, 11),
            Block.createCuboidShape(11, 9, 2, 14, 13, 14),
            Block.createCuboidShape(5, 9, 2, 11, 13, 5)
    ).reduce(VoxelShapes::union).get();

    public AspRiteCoreBlock() {
        super(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AspRiteCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.ASP_RITE_CORE, TickedBlockEntity.ticker());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}
