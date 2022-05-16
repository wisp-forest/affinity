package io.wispforest.affinity.misc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ServerScheduler {

    private static final Multimap<RegistryKey<World>, Task> SCHEDULED = HashMultimap.create();

    private static final List<Consumer<MinecraftServer>> RUN_ONCE = new ArrayList<>();

    public static void runFor(ServerWorld in, int ticks, BooleanSupplier onTick, Runnable whenDone) {
        SCHEDULED.put(in.getRegistryKey(), new Task(ticks, onTick, whenDone));
    }

    public static void runInstantly(Consumer<MinecraftServer> task) {
        RUN_ONCE.add(task);
    }

    static {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!SCHEDULED.containsKey(world.getRegistryKey())) return;
            SCHEDULED.get(world.getRegistryKey()).removeIf(task -> !task.run());
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            RUN_ONCE.forEach(task -> task.accept(server));
            RUN_ONCE.clear();
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
