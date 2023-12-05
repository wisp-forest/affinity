package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.misc.CelestialZoomer;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {

    @Shadow
    private ClientWorld world;

    @Unique
    private RegistryKey<World> affinity$lastWorld = null;

    protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @ModifyArg(method = "onWorldTimeUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;setTimeOfDay(J)V"))
    private long itIsAlwaysNightyNight(long serverTimeOfDay) {
        if (this.world.getChunk(this.client.player.getBlockPos()) instanceof EmptyChunk) return serverTimeOfDay;
        CelestialZoomer.serverTimeOfDay = serverTimeOfDay;

        if (this.affinity$isInWispForest()) {
            final var forestTime = (Math.abs(serverTimeOfDay) / 24000) * -24000 - 18000;

            if (this.affinity$lastWorld != this.world.getRegistryKey()) {
                this.world.setTimeOfDay(forestTime);
            }

            CelestialZoomer.enableOffset(forestTime);
        } else if (CelestialZoomer.offsetEnabled()) {
            CelestialZoomer.disableOffset();
        }

        this.affinity$lastWorld = this.world.getRegistryKey();
        return CelestialZoomer.offsetEnabled() ? this.world.getTimeOfDay() : serverTimeOfDay;
    }

    @Unique
    private boolean affinity$isInWispForest() {
        return Objects.equals(this.world.getBiome(client.player.getBlockPos()).getKey().orElse(null), AffinityWorldgen.WISP_FOREST_KEY);
    }
}
