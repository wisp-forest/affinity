package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LocalWeatherComponent implements Component, ServerTickingComponent {

    private final @NotNull WorldChunk chunk;
    private float rainGradient;
    private float thunderGradient;
    private int ambientDarkness;
    private long lastTick;

    private final Set<BlockPos> monoliths = new HashSet<>();

    public LocalWeatherComponent(Chunk chunk) {
        if (chunk instanceof WorldChunk worldChunk) {
            this.chunk = worldChunk;
        } else {
            //noinspection ConstantConditions
            this.chunk = null;
        }
    }

    public void init() {
        if (this.monoliths.isEmpty()) {
            this.rainGradient = chunk.getWorld().getRainGradient(1);
            this.thunderGradient = chunk.getWorld().getThunderGradient(1);
            this.ambientDarkness = chunk.getWorld().getAmbientDarkness();
        } else {
            this.rainGradient = 0.f;
            this.thunderGradient = 0.f;
            double f = 0.5 + 2.0 * MathHelper.clamp(MathHelper.cos(chunk.getWorld().getSkyAngle(1.0F) * (float) (Math.PI * 2)), -0.25, 0.25);
            this.ambientDarkness = (int) ((1.0 - f) * 11.0);
        }
    }

    public float getRainGradient() {
        this.serverTick();
        return this.rainGradient;
    }

    public float getThunderGradient() {
        this.serverTick();
        return this.thunderGradient;
    }

    public int getAmbientDarkness() {
        this.serverTick();
        return this.ambientDarkness;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.monoliths.clear();

        NbtList monolithsTag = tag.getList("Monoliths", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < monolithsTag.size(); i++) {
            NbtCompound monolithTag = monolithsTag.getCompound(i);

            this.monoliths.add(NbtHelper.toBlockPos(monolithTag));
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList monolithsTag = new NbtList();
        tag.put("Monoliths", monolithsTag);

        for (BlockPos monolithPos : this.monoliths) {
            monolithsTag.add(NbtHelper.fromBlockPos(monolithPos));
        }
    }

    public void addMonolith(BlockPos monolithPos) {
        if (this.monoliths.isEmpty()) {
            this.init();
        }

        this.monoliths.add(monolithPos);
        this.chunk.setNeedsSaving(true);
    }

    public void removeMonolith(BlockPos monolithPos) {
        this.monoliths.remove(monolithPos);
        this.chunk.setNeedsSaving(true);
    }

    @Override
    public void serverTick() {
        var world = chunk.getWorld();

        if (this.lastTick == 0 || this.lastTick > world.getTime()) {
            this.lastTick = world.getTime() - 1;
            init();
        }

        if (this.lastTick == world.getTime()) {
            return;
        }

        if (this.monoliths.isEmpty()) {
            float targetRainGradient = world.isRaining() ? 1.0f : 0.0f;
            float targetThunderGradient = world.isThundering() ? 1.0f : 0.0f;

            this.rainGradient = flatInterpolate(this.rainGradient, targetRainGradient, world.getTime() - this.lastTick);
            this.thunderGradient = flatInterpolate(this.thunderGradient, targetThunderGradient, world.getTime() - this.lastTick);
        } else {
            this.rainGradient = flatInterpolate(this.rainGradient, 0, world.getTime() - this.lastTick);
            this.thunderGradient = flatInterpolate(this.thunderGradient, 0, world.getTime() - this.lastTick);
        }

        if (this.rainGradient == world.getRainGradient(1) && this.thunderGradient == world.getThunderGradient(1)) {
            ambientDarkness = world.getAmbientDarkness();
        } else {
            double d = 1.0 - (double) (this.rainGradient * 5.0F) / 16.0;
            double e = 1.0 - (double) (this.thunderGradient * 5.0F) / 16.0;
            double f = 0.5 + 2.0 * MathHelper.clamp(MathHelper.cos(world.getSkyAngle(1.0F) * (float) (Math.PI * 2)), -0.25, 0.25);
            this.ambientDarkness = (int) ((1.0 - f * d * e) * 11.0);
        }

        this.lastTick = world.getTime();
    }

    public boolean hasMonolith() {
        return !this.monoliths.isEmpty();
    }

    private static float flatInterpolate(float current, float target, long ticksPassed) {
        if (Math.abs(current - target) < 0.01f * (ticksPassed + 1)) {
            return target;
        }

        float newCurrent;

        if (target > current) {
            newCurrent = current + 0.01f * ticksPassed;
        } else {
            newCurrent = current - 0.01f * ticksPassed;
        }

        if (newCurrent > 1.0F || newCurrent < 0.0F) {
            // This is fine.
            // I'm okay with the events that are unfolding currently.
            // That's OK.
            // Things are gonna be OK.

            return target;
        }


        return newCurrent;
    }
}
