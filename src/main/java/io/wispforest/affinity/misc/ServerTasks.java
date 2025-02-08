package io.wispforest.affinity.misc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ServerTasks {

    private static final Multimap<RegistryKey<World>, TickingTask> TICKING = HashMultimap.create();
    private static final Multimap<RegistryKey<World>, ScheduledTask> SCHEDULED = HashMultimap.create();
    private static final List<Consumer<MinecraftServer>> RUN_ONCE = new ArrayList<>();

    public static void doDelayed(ServerWorld in, int delay, Runnable task) {
        SCHEDULED.put(in.getRegistryKey(), new ScheduledTask(delay, task));
    }

    public static void doFor(ServerWorld in, int ticks, TickingTaskRunner onTick, Runnable whenDone) {
        TICKING.put(in.getRegistryKey(), new TickingTask(ticks, onTick, whenDone));
    }

    public static void doFor(ServerWorld in, int ticks, BooleanSupplier onTick, Runnable whenDone) {
        TICKING.put(in.getRegistryKey(), new TickingTask(ticks, $ -> onTick.getAsBoolean(), whenDone));
    }

    public static void doNext(Consumer<MinecraftServer> task) {
        RUN_ONCE.add(task);
    }

    static {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            final var dimension = world.getRegistryKey();
            if (TICKING.containsKey(dimension)) {
                TICKING.get(dimension).removeIf(task -> !task.run());
            }

            if (SCHEDULED.containsKey(dimension)) {
                SCHEDULED.get(dimension).removeIf(task -> {
                    if (task.delay-- > 0) return false;
                    task.action.run();
                    return true;
                });
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            RUN_ONCE.forEach(task -> task.accept(server));
            RUN_ONCE.clear();
        });
    }

    private static final class ScheduledTask {

        private int delay;
        private final Runnable action;

        private ScheduledTask(int delay, Runnable action) {
            this.delay = delay;
            this.action = action;
        }
    }

    private static class TickingTask {

        private int tick = 0;
        private final int length;
        private final TickingTaskRunner onTick;
        private final Runnable whenDone;

        private TickingTask(int length, TickingTaskRunner onTick, Runnable whenDone) {
            this.length = length;
            this.onTick = onTick;
            this.whenDone = whenDone;
        }

        private boolean run() {
            if (tick < length) {
                this.tick++;
                return this.onTick.tick(this.tick);
            } else {
                this.whenDone.run();
                return false;
            }
        }

    }

    @FunctionalInterface
    public interface TickingTaskRunner {
        boolean tick(int tick);
    }
}
