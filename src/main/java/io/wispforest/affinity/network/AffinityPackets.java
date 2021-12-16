package io.wispforest.affinity.network;

import io.wispforest.affinity.Affinity;
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

public class AffinityPackets {

    public static class Server {

        public static final Identifier UPDATE_FLUX = Affinity.id("update_flux");

        public static void sendFluxUpdate(AethumNetworkMemberBlockEntity member) {
            var buf = PacketByteBufs.create();
            buf.writeBlockPos(member.getPos());
            buf.writeVarLong(member.flux());
            PlayerLookup.tracking(member).forEach(player -> ServerPlayNetworking.send(player, UPDATE_FLUX, buf));
        }

    }

    public static class Client {

        public static void registerListeners() {
            ClientPlayNetworking.registerGlobalReceiver(Server.UPDATE_FLUX, Client::onFluxUpdate);
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
