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
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class AffinityPackets {

    public static class Server {

        public static final Identifier UPDATE_FLUX = Affinity.id("update_flux");
        public static final Identifier UPDATE_CACHE_CHILDREN = Affinity.id("update_cache_children");

        public static void sendFluxUpdate(AethumNetworkMemberBlockEntity member) {
            var buf = PacketByteBufs.create();
            buf.writeBlockPos(member.getPos());
            buf.writeVarLong(member.flux());
            PlayerLookup.tracking(member).forEach(player -> ServerPlayNetworking.send(player, UPDATE_FLUX, buf));
        }

        public static void sendCacheChildrenUpdate(AethumFluxCacheBlockEntity cache) {
            var buf = cache.writeChildren();
            buf.writeBlockPos(cache.getPos());
            PlayerLookup.tracking(cache).forEach(player -> ServerPlayNetworking.send(player, UPDATE_CACHE_CHILDREN, buf));
        }

    }

    public static class Client {

        public static void registerListeners() {
            ClientPlayNetworking.registerGlobalReceiver(Server.UPDATE_FLUX, Client::onFluxUpdate);
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
            var pos = byteBuf.readBlockPos();
            long flux = byteBuf.readVarLong();
            client.execute(() -> {
                if (!(client.world.getBlockEntity(pos) instanceof AethumNetworkMemberBlockEntity member)) return;
                member.readFluxUpdate(flux);
            });
        }

    }

}
