package io.wispforest.affinity.misc.components;

import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import io.wispforest.affinity.util.AethumAcquisitionCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.ThreadLocalRandom;

public class ChunkAethumComponent extends AethumComponent<Chunk> implements ServerTickingComponent {

    private static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};

    private final ChunkPos pos;
    private boolean neighborsCached = false;
    private final ChunkAethumComponent[] neighbors = new ChunkAethumComponent[4];

    public ChunkAethumComponent(Chunk chunk) {
        super(AffinityComponents.CHUNK_AETHUM, chunk);
        this.pos = this.holder instanceof WorldChunk ? this.holder.getPos() : ChunkPos.ORIGIN;
    }

    @Override
    public void setAethum(double aethum) {
        super.setAethum(aethum);
        this.holder.setShouldSave(true);
    }

    @Override
    public void serverTick() {
        if (!(this.holder instanceof WorldChunk chunk)) return;
        final var world = chunk.getWorld();

        if (!this.neighborsCached) {
            for (var dir : HORIZONTAL_DIRECTIONS) {
                neighbors[dir.getHorizontal()] = AffinityComponents.CHUNK_AETHUM.get((WorldChunk) world
                        .getChunk(this.pos.x + dir.getOffsetX(), this.pos.z + dir.getOffsetZ(), ChunkStatus.FULL));
            }
            this.neighborsCached = true;
        }

        if (world.getRandom().nextDouble() > .005) return;

        final double previousAethum = this.aethum;

        for (var dir : HORIZONTAL_DIRECTIONS) {
            final var neighbor = neighbors[dir.getHorizontal()];
            if (neighbor == null) continue;

            var diff = this.aethum - neighbor.getAethum();
            if (diff < 20) continue;

            diff *= .5;
            neighbor.addAethum(diff);
            this.aethum -= diff;
        }

        if (this.aethum != previousAethum) this.setAethum(this.aethum);
    }

    public double adjustedAethum() {
        if (!(this.holder instanceof WorldChunk)) return -1;

        double mean = this.aethum;
        double min = this.aethum;
        double max = this.aethum;

        for (var dir : HORIZONTAL_DIRECTIONS) {
            final var neighbor = neighbors[dir.getHorizontal()];
            if (neighbor == null) continue;

            final var aethum = neighbor.getAethum();
            mean += aethum;

            if (aethum < min) min = aethum;
            if (aethum > max) max = aethum;
        }

        mean /= 5;

        if (Math.abs(mean - min) > Math.abs(mean - max)) {
            mean += min * 4;
        } else {
            mean += max * 4;
        }

        return mean / 5;
    }

    public double aethumAt(int x, int z) {
        final var pos = new BlockPos(x, 0, z);

        if (!(this.holder instanceof WorldChunk worldChunk)) return -1;
        final var world = worldChunk.getWorld();

        double numerator = 0;
        double denominator = 0;

        for (x = this.pos.x - 2; x <= this.pos.x + 2; x++) {
            for (z = this.pos.z - 2; z <= this.pos.z + 2; z++) {
                final var aethum = AffinityComponents.CHUNK_AETHUM.get(world.getChunk(x, z)).adjustedAethum();
                final var squaredDistance = pos.getSquaredDistance(getCenter(x, z));

                final var weightedDistance = 1 / (squaredDistance * squaredDistance);
                numerator += weightedDistance * aethum;
                denominator += weightedDistance;
            }
        }

        return numerator / denominator;
    }

    public double fastAethumAt(AethumAcquisitionCache cache, int x, int z) {
        final var pos = new BlockPos(x, 0, z);

        double numerator = 0;
        double denominator = 0;

        for (x = this.pos.x - 2; x <= this.pos.x + 2; x++) {
            for (z = this.pos.z - 2; z <= this.pos.z + 2; z++) {
                final var aethum = cache.getAdjustedAethumFromCache(x, z);
                final var squaredDistance = pos.getSquaredDistance(getCenter(x, z));

                final var weightedDistance = 1 / (squaredDistance * squaredDistance);
                numerator += weightedDistance * aethum;
                denominator += weightedDistance;
            }
        }

        return numerator / denominator;
    }

    private static BlockPos getCenter(int x, int z) {
        return new BlockPos((x << 4) + 8, 0, (z << 4) + 8);
    }

    public ChunkPos getPos() {
        return pos;
    }

    public void regenerate() {
        ThreadLocalRandom.current().nextDouble(50, 75);
        this.holder.setShouldSave(true);
    }

    @Override
    double initialValue() {
        return ThreadLocalRandom.current().nextDouble(60, 85);
    }
}
