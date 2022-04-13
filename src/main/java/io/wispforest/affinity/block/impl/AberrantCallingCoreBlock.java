package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.AberrantCallingCoreBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.isClient()) return;

        for (var neighborPos : possibleNeighbors(pos)) {
            if (noCoreAt(world, neighborPos)) continue;
            makeLine(world, pos, neighborPos);
        }

        for (var diagonal : possibleDiagonals(pos)) {
            if (noCoreAt(world, diagonal)) continue;
            final var differenceVector = diagonal.subtract(pos);

            final var differenceAxes = new Direction[2];
            differenceAxes[0] = differenceVector.getX() > 0 ? Direction.EAST : Direction.WEST;
            differenceAxes[1] = differenceVector.getZ() > 0 ? Direction.SOUTH : Direction.NORTH;

            for (var possibleNeighbor : invertAndOffset(diagonal, differenceAxes)) {
                if (noCoreAt(world, possibleNeighbor)) continue;
                makeLine(world, diagonal, possibleNeighbor);
            }
        }
    }

    private static void makeLine(World world, BlockPos origin, BlockPos target) {
        AffinityParticleSystems.ABERRANT_CORE_HINT.spawn(world, Vec3d.ofCenter(origin), Vec3d.ofCenter(target));
    }

    private static boolean noCoreAt(World world, BlockPos pos) {
        return !world.getBlockState(pos).isOf(AffinityBlocks.ABERRANT_CALLING_CORE);
    }

    private static BlockPos[] possibleDiagonals(BlockPos center) {
        final var positions = possibleNeighbors(center);
        positions[0] = positions[0].add(0, 0, 2);
        positions[1] = positions[1].add(0, 0, -2);
        positions[2] = positions[2].add(-2, 0, 0);
        positions[3] = positions[3].add(2, 0, 0);
        return positions;
    }

    private static BlockPos[] possibleNeighbors(BlockPos center) {
        final var positions = new BlockPos[4];
        positions[0] = center.add(2, 0, 0);
        positions[1] = center.add(-2, 0, 0);
        positions[2] = center.add(0, 0, 2);
        positions[3] = center.add(0, 0, -2);
        return positions;
    }

    private static BlockPos[] invertAndOffset(BlockPos center, Direction[] differenceAxes) {
        final var positions = new BlockPos[2];
        positions[0] = center.offset(differenceAxes[0].getOpposite(), 2);
        positions[1] = center.offset(differenceAxes[1].getOpposite(), 2);
        return positions;
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
