package io.wispforest.affinity.block.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.wispforest.affinity.misc.quack.AffinityChainRestrictedNeighborUpdaterExtension;
import io.wispforest.affinity.mixin.access.WorldAccessor;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

// This class contains quite a lot of the {@link RedstoneWireBlock} code
// verbatim-copied, with only some "up" and "down" swapped
public class RanthraciteWireBlock extends RedstoneWireBlock {

    public static final int[] COLORS = {
            0x171717, 0x191919, 0x1c1c1c, 0x1f1f1f,
            0x222222, 0x252525, 0x272727, 0x2a2a2a,
            0x2d2d2d, 0x303030, 0x333333, 0x353535,
            0x383838, 0x3b3b3b, 0x3e3e3e, 0x414141
    };

    private static final Vec3d[] VEC_COLORS = Arrays.stream(COLORS).mapToObj(rgb ->
            new Vec3d((rgb >> 16) / 255d, ((rgb >> 8) & 0xFF) / 255d, (rgb & 0xFF) / 255d)
    ).toList().toArray(Vec3d[]::new);

    private static final Map<ChunkPos, WeakReference<Chunk>> CHUNK_CACHE = new HashMap<>();

    private static final VoxelShape DOT_SHAPE = Block.createCuboidShape(3.0, 15, 3.0, 13.0, 16, 13.0);

    private static final Map<Direction, VoxelShape> EDGE_SHAPES = Maps.newEnumMap(
            ImmutableMap.of(
                    Direction.NORTH,
                    Block.createCuboidShape(3.0, 15, 0.0, 13.0, 16, 13.0),
                    Direction.SOUTH,
                    Block.createCuboidShape(3.0, 15, 3.0, 13.0, 16, 16.0),
                    Direction.EAST,
                    Block.createCuboidShape(3.0, 15, 3.0, 16.0, 16, 13.0),
                    Direction.WEST,
                    Block.createCuboidShape(0.0, 15, 3.0, 13.0, 16, 13.0)
            )
    );

    private static final Map<Direction, VoxelShape> SIDE_SHAPES = Maps.newEnumMap(
            ImmutableMap.of(
                    Direction.NORTH,
                    VoxelShapes.union(EDGE_SHAPES.get(Direction.NORTH), Block.createCuboidShape(3.0, 0.0, 0.0, 13.0, 16.0, 1.0)),
                    Direction.SOUTH,
                    VoxelShapes.union(EDGE_SHAPES.get(Direction.SOUTH), Block.createCuboidShape(3.0, 0.0, 15.0, 13.0, 16.0, 16.0)),
                    Direction.EAST,
                    VoxelShapes.union(EDGE_SHAPES.get(Direction.EAST), Block.createCuboidShape(15.0, 0.0, 3.0, 16.0, 16.0, 13.0)),
                    Direction.WEST,
                    VoxelShapes.union(EDGE_SHAPES.get(Direction.WEST), Block.createCuboidShape(0.0, 0.0, 3.0, 1.0, 16.0, 13.0))
            )
    );
    private static final Map<BlockState, VoxelShape> SHAPES = Maps.newHashMap();

    private boolean respondToBlockUpdates = true;

    public RanthraciteWireBlock() {
        super(FabricBlockSettings.copyOf(Blocks.REDSTONE_WIRE));

        for (BlockState blockState : this.getStateManager().getStates()) {
            if (blockState.get(POWER) == 0) {
                SHAPES.put(blockState, this.getShapeForState(blockState));
            }
        }
    }

    private static WireConnection getConnection(BlockState state, Direction direction) {
        return state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
    }

    private static BlockState withConnection(BlockState state, Direction direction, WireConnection connection) {
        return state.with(getConnectionProp(direction), connection);
    }

    private static EnumProperty<WireConnection> getConnectionProp(Direction direction) {
        return DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction);
    }

    private VoxelShape getShapeForState(BlockState state) {
        var shape = DOT_SHAPE;

        for (Direction direction : Direction.Type.HORIZONTAL) {
            var connection = getConnection(state, direction);
            if (connection == WireConnection.SIDE) {
                shape = VoxelShapes.union(shape, EDGE_SHAPES.get(direction));
            } else if (connection == WireConnection.UP) {
                shape = VoxelShapes.union(shape, SIDE_SHAPES.get(direction));
            }
        }

        return shape;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        final var up = pos.up();
        return this.canRunOnTop(world, up, world.getBlockState(up));
    }

    @Override
    protected boolean canRunOnTop(BlockView world, BlockPos pos, BlockState floor) {
        return floor.isSideSolidFullSquare(world, pos, Direction.DOWN);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.with(POWER, 0));
    }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        if (direction == Direction.UP) {
            return state;
        } else if (direction == Direction.DOWN) {
            return this.getPlacementState(world, state, pos);
        } else {
            var connection = this.getRenderConnectionType(world, pos, direction);
            return connection.isConnected() == getConnection(state, direction).isConnected()
                    && !isFullyConnected(state)
                    ? withConnection(state, direction, connection)
                    : this.getPlacementState(
                    world, withConnection(this.dotState.with(POWER, state.get(POWER)), direction, connection), pos
            );
        }
    }

    @Override
    protected BlockState getDefaultWireState(BlockView world, BlockState state, BlockPos pos) {
        boolean solidBelow = !world.getBlockState(pos.down()).isSolidBlock(world, pos);

        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (!getConnection(state, direction).isConnected()) {
                var connection = this.getRenderConnectionType(world, pos, direction, solidBelow);
                state = withConnection(state, direction, connection);
            }
        }

        return state;
    }

    @Override
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        final var mutable = new BlockPos.Mutable();

        for (Direction direction : Direction.Type.HORIZONTAL) {
            WireConnection wireConnection = getConnection(state, direction);
            if (wireConnection != WireConnection.NONE && !world.getBlockState(mutable.set(pos, direction)).isOf(this)) {
                mutable.move(Direction.UP);
                BlockState blockState = world.getBlockState(mutable);
                if (!blockState.isOf(Blocks.OBSERVER)) {
                    BlockPos blockPos = mutable.offset(direction.getOpposite());
                    BlockState blockState2 = blockState.getStateForNeighborUpdate(direction.getOpposite(), world.getBlockState(blockPos), world, mutable, blockPos);
                    replace(blockState, blockState2, world, mutable, flags, maxUpdateDepth);
                }

                mutable.set(pos, direction).move(Direction.DOWN);
                BlockState blockState3 = world.getBlockState(mutable);
                if (!blockState3.isOf(Blocks.OBSERVER)) {
                    BlockPos blockPos2 = mutable.offset(direction.getOpposite());
                    BlockState blockState4 = blockState3.getStateForNeighborUpdate(direction.getOpposite(), world.getBlockState(blockPos2), world, mutable, blockPos2);
                    replace(blockState3, blockState4, world, mutable, flags, maxUpdateDepth);
                }
            }
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!this.respondToBlockUpdates) return;
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }

    @Override
    protected void update(World world, BlockPos pos, BlockState state) {
        final int previousPower = state.get(POWER);

        this.wiresGivePower = false;
//        if (previousPower == world.getReceivedRedstonePower(pos)) {
//            this.wiresGivePower = true;
//            return;
//        }

        final var foundNodes = new ArrayList<BlockPos>();
        final var searchQueue = new ArrayDeque<BlockPos>();

        searchQueue.add(pos);

        while (!searchQueue.isEmpty()) {
            final var nextPos = searchQueue.poll();
            if (!world.isChunkLoaded(nextPos)) continue;
            if (searchQueue.contains(nextPos) || foundNodes.contains(nextPos)) continue;

            final var nextState = CHUNK_CACHE.computeIfAbsent(new ChunkPos(nextPos), chunkPos -> new WeakReference<>(world.getChunk(nextPos)))
                    .get().getBlockState(nextPos);
            if (!nextState.isOf(this)) continue;
            foundNodes.add(nextPos);

            for (var direction : Direction.Type.HORIZONTAL) {
                final var connection = nextState.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
                if (!connection.isConnected()) continue;

                if (connection == WireConnection.SIDE) {
                    searchQueue.add(nextPos.offset(direction));
                    searchQueue.add(nextPos.offset(direction).up());
                } else if (connection == WireConnection.UP) {
                    searchQueue.add(nextPos.offset(direction).offset(Direction.DOWN));
                }
            }

        }

        int networkPower = 0;

        for (var node : foundNodes) {
            networkPower = Math.max(networkPower, world.getReceivedRedstonePower(node));
        }
        this.wiresGivePower = true;

        if (networkPower != previousPower) {
            this.respondToBlockUpdates = false;

            if (((WorldAccessor) world).affinity$getNeighborUpdater() instanceof AffinityChainRestrictedNeighborUpdaterExtension extension) {
                extension.affinity$beginGroup();
            }

            for (var node : foundNodes) {
                world.setBlockState(node, CHUNK_CACHE.get(new ChunkPos(node)).get().getBlockState(node).with(POWER, networkPower));
                for (var dir : DIRECTIONS) {
                    world.updateNeighborsAlways(node.offset(dir), this);
                }
            }

            if (((WorldAccessor) world).affinity$getNeighborUpdater() instanceof AffinityChainRestrictedNeighborUpdaterExtension extension) {
                extension.affinity$submitGroup();
            }

            this.respondToBlockUpdates = true;
        }
    }

    @Override
    protected WireConnection getRenderConnectionType(BlockView world, BlockPos pos, Direction direction) {
        return this.getRenderConnectionType(world, pos, direction, !world.getBlockState(pos.down()).isSolidBlock(world, pos));
    }

    @Override
    protected WireConnection getRenderConnectionType(BlockView world, BlockPos pos, Direction direction, boolean solidBelow) {
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        if (solidBelow) {
            boolean validPlacement = this.canRunOnTop(world, blockPos, blockState);
            if (validPlacement && connectsTo(world.getBlockState(blockPos.down()))) {
                if (blockState.isSideSolidFullSquare(world, blockPos, direction.getOpposite())) {
                    return WireConnection.UP;
                }

                return WireConnection.SIDE;
            }
        }

        return !connectsTo(blockState, direction) && (blockState.isSolidBlock(world, blockPos) || !connectsTo(world.getBlockState(blockPos.up())))
                ? WireConnection.NONE : WireConnection.SIDE;
    }


    @Override
    protected void updateOffsetNeighbors(World world, BlockPos pos) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            this.updateNeighbors(world, pos.offset(direction));
        }

        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos blockPos = pos.offset(direction);
            if (world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {
                this.updateNeighbors(world, blockPos.down());
            } else {
                this.updateNeighbors(world, blockPos.up());
            }
        }
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (this.wiresGivePower && direction != Direction.UP) {
            int i = state.get(POWER);
            if (i == 0) {
                return 0;
            } else {
                return direction != Direction.DOWN
                        && !this.getPlacementState(world, state, pos).get(getConnectionProp(direction.getOpposite())).isConnected() ? 0 : i;
            }
        } else {
            return 0;
        }
    }

    private void addPoweredParticles(World world, Random random, BlockPos pos, Vec3d color, Direction direction, Direction direction2, float f, float g) {
        float h = g - f;
        if (!(random.nextFloat() >= 0.2F * h)) {

            float randomOffset = f + h * random.nextFloat();

            double x = 0.5 + (0.4375F * direction.getOffsetX()) + (randomOffset * direction2.getOffsetX());
            double y = 0.5 + (0.4375F * direction.getOffsetY()) + (randomOffset * direction2.getOffsetY());
            double z = 0.5 + (0.4375F * direction.getOffsetZ()) + (randomOffset * direction2.getOffsetZ());

            world.addParticle(new DustParticleEffect(color.toVector3f(), 1), pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                    0.0, 0.0, 0.0);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        int i = state.get(POWER);
        if (i != 0) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                var connection = state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
                switch (connection) {
                    case UP:
                        this.addPoweredParticles(world, random, pos, VEC_COLORS[i], direction, Direction.UP, -0.5F, 0.5F);
                    case SIDE:
                        this.addPoweredParticles(world, random, pos, VEC_COLORS[i], Direction.UP, direction, 0.0F, 0.5F);
                        break;
                    default:
                        this.addPoweredParticles(world, random, pos, VEC_COLORS[i], Direction.UP, direction, 0.0F, 0.3F);
                }
            }
        }
    }

    protected static boolean connectsTo(BlockState state) {
        return connectsTo(state, null);
    }

    protected static boolean connectsTo(BlockState state, @Nullable Direction dir) {
        if (state.isOf(AffinityBlocks.RANTHRACITE_WIRE)) {
            return true;
        } else if (state.isOf(Blocks.REPEATER)) {
            Direction direction = state.get(RepeaterBlock.FACING);
            return direction == dir || direction.getOpposite() == dir;
        } else if (state.isOf(Blocks.OBSERVER)) {
            return dir == state.get(ObserverBlock.FACING);
        } else {
            return state.emitsRedstonePower() && dir != null;
        }
    }
}
