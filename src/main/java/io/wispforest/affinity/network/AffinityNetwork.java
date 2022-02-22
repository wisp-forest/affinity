package io.wispforest.affinity.network;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.owo.network.OwoNetChannel;

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

        RitualSocleComposerScreenHandler.initNetwork();
    }

}
