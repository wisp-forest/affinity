package io.wispforest.affinity.network;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;

public class AffinityNetwork {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(Affinity.id("main"));

    public static void initialize() {
        PacketBufSerializer.register(ChunkPos.class, PacketByteBuf::writeChunkPos, PacketByteBuf::readChunkPos);
        CHANNEL.registerClientbound(FluxSyncHandler.FluxSyncPacket.class, (message, access) -> {
            final var chunk = access.runtime().world.getChunk(message.chunk().x, message.chunk().z);
            message.updates().forEach((pos, flux) -> {
                if (!(chunk.getBlockEntity(pos) instanceof AethumNetworkMemberBlockEntity member)) return;
                member.readFluxUpdate(flux);
            });
        });
    }

}
