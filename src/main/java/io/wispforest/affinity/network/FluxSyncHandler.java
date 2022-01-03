package io.wispforest.affinity.network;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class FluxSyncHandler {

    private static final Map<RegistryKey<World>, Map<ChunkPos, Map<BlockPos, Long>>> PENDING_UPDATES = new HashMap<>();

    public static void queueUpdate(AethumNetworkMemberBlockEntity entity) {
        getPendingForWorld(entity.getWorld()).computeIfAbsent(new ChunkPos(entity.getPos()), chunkPos -> new HashMap<>()).put(entity.getPos(), entity.flux());
    }

    private static void dispatchUpdatesToClients(ServerWorld world) {
        final var pendingForWorld = getPendingForWorld(world);
        if (pendingForWorld.isEmpty()) return;

        pendingForWorld.forEach((chunk, updates) -> AffinityPackets.Server.sendChunkFluxUpdates(world, chunk, updates));
        pendingForWorld.clear();
    }

    private static Map<ChunkPos, Map<BlockPos, Long>> getPendingForWorld(World world) {
        return PENDING_UPDATES.computeIfAbsent(world.getRegistryKey(), worldRegistryKey -> new HashMap<>());
    }

    static {
        ServerTickEvents.END_WORLD_TICK.register(FluxSyncHandler::dispatchUpdatesToClients);
    }
}