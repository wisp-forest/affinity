package io.wispforest.affinity.network;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.template.AttackInteractionReceiver;
import io.wispforest.affinity.block.template.ScrollInteractionReceiver;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public class AffinityNetwork {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(Affinity.id("main"));

    public static OwoNetChannel.ServerHandle server(BlockEntity entity) {
        return CHANNEL.serverHandle(entity);
    }

    public static OwoNetChannel.ServerHandle server(PlayerEntity entity) {
        return CHANNEL.serverHandle(entity);
    }

    public static OwoNetChannel.ServerHandle server(MinecraftServer server) {
        return CHANNEL.serverHandle(server);
    }

    public static OwoNetChannel.ServerHandle server(Collection<ServerPlayerEntity> players) {
        return CHANNEL.serverHandle(players);
    }

    public static OwoNetChannel.ServerHandle server(ServerWorld world, BlockPos pos) {
        return CHANNEL.serverHandle(world, pos);
    }

    public static void initialize() {
        CHANNEL.registerClientbound(FluxSyncHandler.FluxSyncPacket.class, (message, access) -> {
            final var chunk = access.runtime().world.getChunk(message.chunk().x, message.chunk().z);
            message.updates().forEach((pos, flux) -> {
                if (!(chunk.getBlockEntity(pos) instanceof AethumNetworkMemberBlockEntity member)) return;
                member.readFluxUpdate(flux);
            });
        });

        CHANNEL.registerServerbound(ScrollInteractionReceiver.InteractionPacket.class, (message, access) -> {
            var player = access.player();
            var world = player.getWorld();

            if (!world.canPlayerModifyAt(player, message.pos())) return;

            var state = world.getBlockState(message.pos());
            if (!(state.getBlock() instanceof ScrollInteractionReceiver receiver)) return;

            receiver.onScroll(world, state, message.pos(), player, message.direction());
            player.swingHand(Hand.MAIN_HAND);
        });

        CHANNEL.registerServerbound(AttackInteractionReceiver.InteractionPacket.class, (message, access) -> {
            var player = access.player();
            var world = player.getWorld();

            if (!world.canPlayerModifyAt(player, message.pos())) return;

            var state = world.getBlockState(message.pos());
            if (!(state.getBlock() instanceof AttackInteractionReceiver receiver)) return;

            receiver.onAttack(world, state, message.pos(), player);
            player.swingHand(Hand.MAIN_HAND);
        });

        RitualSocleComposerScreenHandler.initNetwork();
    }

}
