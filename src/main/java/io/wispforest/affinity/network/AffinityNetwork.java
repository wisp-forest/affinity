package io.wispforest.affinity.network;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;

public class AffinityNetwork {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(Affinity.id("main"));

    public static void initialize() {
        CHANNEL.registerClientbound(FluxSyncHandler.FluxSyncPacket.class, (message, access) -> {
            final var chunk = access.runtime().world.getChunk(message.chunk().x, message.chunk().z);
            message.updates().forEach((pos, flux) -> {
                if (!(chunk.getBlockEntity(pos) instanceof AethumNetworkMemberBlockEntity member)) return;
                member.readFluxUpdate(flux);
            });
        });
    }

    static {
        PacketBufSerializer.register(ChunkPos.class, PacketByteBuf::writeChunkPos, PacketByteBuf::readChunkPos);

        PacketBufSerializer.register(ParticleEffect.class, (buf, particleEffect) -> {
            buf.writeInt(Registry.PARTICLE_TYPE.getRawId(particleEffect.getType()));
            particleEffect.write(buf);
        }, buf -> {
            //noinspection rawtypes
            final ParticleType particleType = Registry.PARTICLE_TYPE.get(buf.readInt());
            //noinspection unchecked, ConstantConditions
            return particleType.getParametersFactory().read(particleType, buf);
        });
    }

}
