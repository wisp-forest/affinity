package io.wispforest.affinity.misc.components;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ChunkAethumComponent extends AethumComponent<Chunk> implements ServerTickingComponent {

    private static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};
    private static final Cache<ChunkPos, WorldChunk> CHUNK_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100).expireAfterAccess(60, TimeUnit.SECONDS).build();

    private final ChunkPos pos;

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

        if (world.getRandom().nextDouble() > .005) return;

        final double previousAethum = this.aethum;

        for (var dir : HORIZONTAL_DIRECTIONS) {
            final var aethumComponent = AffinityComponents.CHUNK_AETHUM.get(getChunk(world, this.pos, dir));
            var diff = this.aethum - aethumComponent.getAethum();
            if (diff < 20) continue;

            diff *= .15;
            aethumComponent.addAethum(diff);
            this.aethum -= diff;
        }

        if (this.aethum != previousAethum) this.setAethum(this.aethum);
    }

    public double adjustedAethum() {
        if (!(this.holder instanceof WorldChunk worldChunk)) return -1;
        var world = worldChunk.getWorld();

        double mean = this.aethum;
        double min = this.aethum;
        double max = this.aethum;
        for (var dir : HORIZONTAL_DIRECTIONS) {
            final var aethum = AffinityComponents.CHUNK_AETHUM.get(getChunk(world, this.pos, dir)).getAethum();
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

    public double aethumAt(BlockPos pos) {
        pos = new BlockPos(pos.getX(), 0, pos.getZ());

        if (!(this.holder instanceof WorldChunk worldChunk)) return -1;
        var world = worldChunk.getWorld();
        final var chunks = new Chunk[25];

        int idx = 0;
        for (int x = this.pos.x - 2; x <= this.pos.x + 2; x++) {
            for (int z = this.pos.z - 2; z <= this.pos.z + 2; z++) {
                chunks[idx++] = getCached(world, new ChunkPos(x, z));
            }
        }

        double numerator = 0;
        double denominator = 0;

        for (var chunk : chunks) {
            final var distance = Math.pow(Math.sqrt(pos.getSquaredDistance(getCenter(chunk))), -3.5);
            numerator += distance * AffinityComponents.CHUNK_AETHUM.get(chunk).adjustedAethum();
            denominator += distance;
        }

        return numerator / denominator;
    }

    private static WorldChunk getChunk(World world, ChunkPos pos, Direction offset) {
        return getCached(world, new ChunkPos(pos.x + offset.getOffsetX(), pos.z + offset.getOffsetZ()));
    }

    private static WorldChunk getCached(World world, ChunkPos pos) {
        try {
            return CHUNK_CACHE.get(pos, () -> world.getChunk(pos.x, pos.z));
        } catch (ExecutionException e) {
            return world.getChunk(pos.x, pos.z);
        }
    }

    private static BlockPos getCenter(Chunk chunk) {
        final var pos = chunk.getPos();
        return new BlockPos(pos.x * 16 + 8, 0, pos.z * 16 + 8);
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
