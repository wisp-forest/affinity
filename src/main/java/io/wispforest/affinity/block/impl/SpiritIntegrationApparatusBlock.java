package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.RitualCoreBlock;
import io.wispforest.affinity.blockentity.impl.SpiritIntegrationApparatusBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityCriteria;
import io.wispforest.affinity.object.AffinityParticleSystems;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class SpiritIntegrationApparatusBlock extends RitualCoreBlock {

    private static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(4, 4, 4, 12, 12, 12),
            Block.createCuboidShape(3, 1, 3, 13, 4, 13),
            Block.createCuboidShape(4, 0, 4, 12, 1, 12),
            Block.createCuboidShape(2, 2, 6, 4, 6, 10),
            Block.createCuboidShape(12, 2, 6, 14, 6, 10),
            Block.createCuboidShape(6, 2, 2, 10, 6, 4),
            Block.createCuboidShape(6, 2, 12, 10, 6, 14)
    ).reduce(VoxelShapes::union).get();

    public SpiritIntegrationApparatusBlock() {
        super(FabricBlockSettings.copyOf(Blocks.RED_NETHER_BRICKS));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.isClient()) return;

        for (var neighborPos : possibleNeighbors(pos)) {
            if (noApparatusAt(world, neighborPos)) continue;
            makeLine(world, pos, neighborPos);
        }

        for (var diagonal : possibleDiagonals(pos)) {
            if (noApparatusAt(world, diagonal)) continue;
            final var differenceVector = diagonal.subtract(pos);

            final var differenceAxes = new Direction[2];
            differenceAxes[0] = differenceVector.getX() > 0 ? Direction.EAST : Direction.WEST;
            differenceAxes[1] = differenceVector.getZ() > 0 ? Direction.SOUTH : Direction.NORTH;

            for (var possibleNeighbor : invertAndOffset(diagonal, differenceAxes)) {
                if (noApparatusAt(world, possibleNeighbor)) continue;
                makeLine(world, diagonal, possibleNeighbor);
            }
        }

        if (placer instanceof ServerPlayerEntity player) {
            int completion = 0;

            for (var set : possibleValidApparatusSets(pos)) {
                completion = Math.max(completion, set.validApparatusCount(world));
            }

            AffinityCriteria.CONSTRUCT_SPIRIT_INTEGRATION_APPARATUS.trigger(player, completion + 1);
        }
    }

    private static void makeLine(World world, BlockPos origin, BlockPos target) {
        AffinityParticleSystems.SPIRIT_INTEGRATION_APPARATUS_HINT.spawn(world, Vec3d.ofCenter(origin), Vec3d.ofCenter(target));
    }

    private static boolean noApparatusAt(World world, BlockPos pos) {
        return !world.getBlockState(pos).isOf(AffinityBlocks.SPIRIT_INTEGRATION_APPARATUS);
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

    public static @Nullable SpiritIntegrationApparatusBlock.ApparatusSet findValidApparatusSet(World world, BlockPos corner) {
        final var possibleSets = possibleValidApparatusSets(corner);
        for (var set : possibleSets) {
            if (set.hasMissingApparatuses(world)) continue;
            return set;
        }
        return null;
    }

    public static List<ApparatusSet> possibleValidApparatusSets(BlockPos corner) {
        final var list = new ArrayList<ApparatusSet>();
        for (var direction : HORIZONTAL_DIRECTIONS) {
            list.add(new ApparatusSet(new BlockPos[]{
                    corner.offset(direction, 2),
                    corner.offset(direction, 2).offset(direction.rotateYClockwise(), 2),
                    corner.offset(direction.rotateYClockwise(), 2)
            }, corner.offset(direction).offset(direction.rotateYClockwise())));
        }
        return list;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, AffinityBlocks.Entities.SPIRIT_INTEGRATION_APPARATUS, TickedBlockEntity.ticker());
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
        return new SpiritIntegrationApparatusBlockEntity(pos, state);
    }

    public record ApparatusSet(BlockPos[] apparatuses, BlockPos center) implements Iterable<BlockPos> {

        public boolean hasMissingApparatuses(World world) {
            return this.validApparatusCount(world) != 3;
        }

        public int validApparatusCount(World world) {
            int validApparatuses = 0;

            for (var pos : this.apparatuses) {
                if (SpiritIntegrationApparatusBlock.noApparatusAt(world, pos)) continue;
                validApparatuses++;
            }

            return validApparatuses;
        }

        public SpiritIntegrationApparatusBlockEntity[] resolve(World world) {
            final var resolved = new SpiritIntegrationApparatusBlockEntity[3];
            for (int i = 0; i < apparatuses.length; i++) {
                resolved[i] = (SpiritIntegrationApparatusBlockEntity) world.getBlockEntity(apparatuses[i]);
            }
            return resolved;
        }

        public BlockPos get(int idx) {
            return this.apparatuses[idx];
        }

        @NotNull
        @Override
        public Iterator<BlockPos> iterator() {
            return List.of(this.apparatuses).iterator();
        }
    }
}
