package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.AberrantCallingCoreBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
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

public class AberrantCallingCoreBlock extends AethumNetworkMemberBlock {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(5, 4, 12, 11, 11, 13),
            Block.createCuboidShape(2, 0, 2, 14, 3, 14),
            Block.createCuboidShape(4, 3, 4, 12, 12, 12),
            Block.createCuboidShape(3, 4, 5, 4, 11, 11),
            Block.createCuboidShape(12, 4, 5, 13, 11, 11),
            Block.createCuboidShape(5, 4, 3, 11, 11, 4)
    ).reduce(VoxelShapes::union).get();

    public AberrantCallingCoreBlock() {
        super(FabricBlockSettings.copyOf(Blocks.RED_NETHER_BRICKS));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AberrantCallingCoreBlockEntity(pos, state);
    }
}
