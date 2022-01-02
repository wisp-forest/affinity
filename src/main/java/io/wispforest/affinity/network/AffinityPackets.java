package io.wispforest.affinity.network;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.AethumFluxCacheBlockEntity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Map;

public class AffinityPackets {

    public static class Server {

        public static final Identifier UPDATE_CHUNK_FLUX = Affinity.id("update_chunk_flux");
        public static final Identifier UPDATE_CACHE_CHILDREN = Affinity.id("update_cache_children");

        public static void sendChunkFluxUpdates(ServerWorld world, ChunkPos chunk, Map<BlockPos, Long> updates) {
            var buf = PacketByteBufs.create();
            buf.writeVarLong(chunk.toLong());
            buf.writeMap(updates, PacketByteBuf::writeBlockPos, PacketByteBuf::writeVarLong);
            PlayerLookup.tracking(world, chunk).forEach(player -> ServerPlayNetworking.send(player, UPDATE_CHUNK_FLUX, buf));
        }

        public static void sendCacheChildrenUpdate(AethumFluxCacheBlockEntity cache) {
            var buf = cache.writeChildren();
            buf.writeBlockPos(cache.getPos());
            PlayerLookup.tracking(cache).forEach(player -> ServerPlayNetworking.send(player, UPDATE_CACHE_CHILDREN, buf));
        }

    }

    public static class Client {

        public static void registerListeners() {
            ClientPlayNetworking.registerGlobalReceiver(Server.UPDATE_CHUNK_FLUX, Client::onFluxUpdate);
            ClientPlayNetworking.registerGlobalReceiver(Server.UPDATE_CACHE_CHILDREN, Client::onCacheChildrenUpdate);
        }

        private static void onCacheChildrenUpdate(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf byteBuf, PacketSender packetSender) {
            var children = byteBuf.readCollection(value -> new ArrayList<>(), PacketByteBuf::readBlockPos);
            var cachePos = byteBuf.readBlockPos();
            client.execute(() -> {
                if (!(client.world.getBlockEntity(cachePos) instanceof AethumFluxCacheBlockEntity cache)) return;
                cache.readChildren(children);
            });
        }

        private static void onFluxUpdate(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf byteBuf, PacketSender packetSender) {
            final var chunkPos = new ChunkPos(byteBuf.readVarLong());
            final var updates = byteBuf.readMap(PacketByteBuf::readBlockPos, PacketByteBuf::readVarLong);
            client.execute(() -> {
                final var chunk = client.world.getChunk(chunkPos.x, chunkPos.z);
                updates.forEach((pos, flux) -> {
                    if (!(chunk.getBlockEntity(pos) instanceof AethumNetworkMemberBlockEntity member)) return;
                    member.readFluxUpdate(flux);
                });
            });
        }

    }

}
