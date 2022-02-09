package io.wispforest.affinity.misc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.function.BooleanSupplier;

public class ServerExecutionScheduler {

    private static final Multimap<RegistryKey<World>, Task> TASKS = HashMultimap.create();

    public static void runFor(ServerWorld in, int ticks, BooleanSupplier onTick, Runnable whenDone) {
        TASKS.put(in.getRegistryKey(), new Task(ticks, onTick, whenDone));
    }

    static {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!TASKS.containsKey(world.getRegistryKey())) return;
            TASKS.get(world.getRegistryKey()).removeIf(task -> !task.run());
        });
    }

    private static class Task {

        private int tick = 0;
        private final int length;
        private final BooleanSupplier onTick;
        private final Runnable whenDone;

        private Task(int length, BooleanSupplier onTick, Runnable whenDone) {
            this.length = length;
            this.onTick = onTick;
            this.whenDone = whenDone;
        }

        private boolean run() {
            if (tick < length) {
                this.tick++;
                return this.onTick.getAsBoolean();
            } else {
                this.whenDone.run();
                return false;
            }
        }

    }

}
