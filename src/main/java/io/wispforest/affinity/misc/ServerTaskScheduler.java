package io.wispforest.affinity.misc;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ServerTaskScheduler {
    private static final List<Consumer<MinecraftServer>> TASKS = new ArrayList<>();
    
    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var task : TASKS) {
                task.accept(server);
            }

            TASKS.clear();
        });
    }

    public static void scheduleTask(Consumer<MinecraftServer> task) {
        TASKS.add(task);
    }
}
