package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.misc.util.BlockFinder;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ArcaneTreetapBlock extends HorizontalFacingBlock {

    public static final DustColorTransitionParticleEffect PARTICLE = new DustColorTransitionParticleEffect(MathUtil.splitRGBToVec3f(0x865DFF), MathUtil.splitRGBToVec3f(0xFFA3FD), 1f);

    private static final VoxelShape NORTH_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(6, 6, 2, 10, 10, 5),
            Block.createCuboidShape(5, 5, 0, 11, 11, 2)
    );

    private static final VoxelShape SOUTH_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(6, 6, 11, 10, 10, 14),
            Block.createCuboidShape(5, 5, 14, 11, 11, 16)
    );

    public static final VoxelShape WEST_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(2, 6, 6, 5, 10, 10),
            Block.createCuboidShape(0, 5, 5, 2, 11, 11)
    );

    public static final VoxelShape EAST_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(11, 6, 6, 14, 10, 10),
            Block.createCuboidShape(14, 5, 5, 16, 11, 11)
    );

    public ArcaneTreetapBlock() {
        super(FabricBlockSettings.copyOf(Blocks.OAK_TRAPDOOR));
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.north()).isIn(BlockTags.LOGS)
                || world.getBlockState(pos.south()).isIn(BlockTags.LOGS)
                || world.getBlockState(pos.east()).isIn(BlockTags.LOGS)
                || world.getBlockState(pos.west()).isIn(BlockTags.LOGS);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction != state.get(FACING)) return state;
        return world.getBlockState(neighborPos).isIn(BlockTags.LOGS) ? state : Blocks.AIR.getDefaultState();
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var side = ctx.getSide();
        if (side.getAxis() == Direction.Axis.Y) side = Direction.NORTH;

        return this.getDefaultState().with(FACING, side.getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            default -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
        };
    }

    @Override
    public ActionResult onUse(BlockState clickedState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        if (isProperlyAttached(world, pos)) {
            player.sendMessage(Text.literal("yep, that is in fact a tree").formatted(Formatting.GREEN), false);
        } else {
            player.sendMessage(Text.literal("nope, not a tree. no.").formatted(Formatting.RED), false);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        boolean attached = isProperlyAttached(world, pos);
        var facing = state.get(FACING);

        if (attached) {
            ClientParticles.spawn(PARTICLE, world, Vec3d.ofCenter(pos).add(facing.getOffsetX() * .25, 0, facing.getOffsetZ() * .25), .15f);
            ClientParticles.spawn(new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, Blocks.AMETHYST_BLOCK.getDefaultState()), world, Vec3d.ofCenter(pos).add(facing.getOffsetX() * .25, 0, facing.getOffsetZ() * .25), .15f);
        } else {
            ClientParticles.spawn(ParticleTypes.SMOKE, world, Vec3d.ofCenter(pos).add(facing.getOffsetX() * .25, 0, facing.getOffsetZ() * .25), .15f);
        }
    }

    public static Set<BlockPos> walkConnectedTree(World world, BlockPos pos) {
        return BlockFinder.findCapped(world, pos.offset(world.getBlockState(pos).get(FACING)), (blockPos, state) -> state.isIn(BlockTags.LOGS), 128).results().keySet();
    }

    public static boolean isProperlyAttached(World world, BlockPos pos) {
        final var results = BlockFinder.findCapped(world, pos.offset(world.getBlockState(pos).get(FACING)), (blockPos, state) -> {
            if (state.isIn(BlockTags.LEAVES)) {
                return !state.get(LeavesBlock.PERSISTENT);
            }

            return state.isIn(BlockTags.LOGS);
        }, 128);
        final var counted = results.byCount();

        int logCount = counted.keySet().stream()
                .filter(block -> block.getRegistryEntry().isIn(BlockTags.LOGS))
                .mapToInt(counted::get).sum();
        int leavesCount = counted.keySet().stream()
                .filter(block -> block.getRegistryEntry().isIn(BlockTags.LEAVES))
                .mapToInt(counted::get).sum();

        return logCount > 5 && leavesCount > 40;
    }
}
