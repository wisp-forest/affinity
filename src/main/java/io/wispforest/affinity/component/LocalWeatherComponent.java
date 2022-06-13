package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LocalWeatherComponent implements Component, ServerTickingComponent {
    private final @NotNull WorldChunk c;
    private float rainGradient;
    private float thunderGradient;
    private int ambientDarkness;
    private long lastTick;

    private final Set<BlockPos> monoliths = new HashSet<>();

    public LocalWeatherComponent(Chunk c) {
        if (c instanceof WorldChunk wc) {
            this.c = wc;
        } else {
            //noinspection ConstantConditions
            this.c = null;
        }
    }

    public void init() {
        rainGradient = c.getWorld().getRainGradient(1);
        thunderGradient = c.getWorld().getThunderGradient(1);
        ambientDarkness = c.getWorld().getAmbientDarkness();
    }

    public float getRainGradient() {
        serverTick();
        return rainGradient;
    }

    public float getThunderGradient() {
        serverTick();
        return thunderGradient;
    }

    public int getAmbientDarkness() {
        serverTick();
        return ambientDarkness;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        monoliths.clear();

        NbtList monolithsTag = tag.getList("Monoliths", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < monolithsTag.size(); i++) {
            NbtCompound monolithTag = monolithsTag.getCompound(i);

            monoliths.add(NbtHelper.toBlockPos(monolithTag));
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList monolithsTag = new NbtList();
        tag.put("Monoliths", monolithsTag);

        for (BlockPos monolithPos : monoliths) {
            monolithsTag.add(NbtHelper.fromBlockPos(monolithPos));
        }
    }

    public void addMonolith(BlockPos monolithPos) {
        if (monoliths.isEmpty()) {
            init();
        }

        monoliths.add(monolithPos);
        c.setNeedsSaving(true);
    }

    public void removeMonolith(BlockPos monolithPos) {
        monoliths.remove(monolithPos);
        c.setNeedsSaving(true);
    }

    private static float flatInterpolate(float current, float target, long ticksPassed) {
        if (Math.abs(current - target) < 0.01f * (ticksPassed + 1))
            return target;

        float newCurrent;

        if (target > current)
            newCurrent = current + 0.01f * ticksPassed;
        else
            newCurrent = current - 0.01f * ticksPassed;

        if (newCurrent > 1.0F || newCurrent < 0.0F) {
            // This is fine.
            // I'm okay with the events that are unfolding currently.
            // That's OK.
            // Things are gonna be OK.

            return target;
        }


        return newCurrent;
    }

    @Override
    public void serverTick() {
        World w = c.getWorld();

        if (lastTick == 0 || lastTick > w.getTime())
            lastTick = w.getTime() - 1;

        if (lastTick == w.getTime())
            return;

        if (monoliths.isEmpty()) {
            float targetRainGradient = w.isRaining() ? 1.0f : 0.0f;
            float targetThunderGradient = w.isThundering() ? 1.0f : 0.0f;

            rainGradient = flatInterpolate(rainGradient, targetRainGradient, w.getTime() - lastTick);
            thunderGradient = flatInterpolate(thunderGradient, targetThunderGradient, w.getTime() - lastTick);
        } else {
            rainGradient = flatInterpolate(rainGradient, 0, w.getTime() - lastTick);
            thunderGradient = flatInterpolate(thunderGradient, 0, w.getTime() - lastTick);
        }

        if (rainGradient == w.getRainGradient(1) && thunderGradient == w.getThunderGradient(1)) {
            ambientDarkness = w.getAmbientDarkness();
        } else {
            double d = 1.0 - (double)(rainGradient * 5.0F) / 16.0;
            double e = 1.0 - (double)(thunderGradient * 5.0F) / 16.0;
            double f = 0.5 + 2.0 * MathHelper.clamp(MathHelper.cos(w.getSkyAngle(1.0F) * (float) (Math.PI * 2)), -0.25, 0.25);
            ambientDarkness = (int)((1.0 - f * d * e) * 11.0);
        }

        lastTick = w.getTime();
    }
}
