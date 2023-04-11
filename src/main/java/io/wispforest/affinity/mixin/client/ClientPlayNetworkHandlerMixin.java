package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.misc.CelestialZoomer;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow
    private ClientWorld world;

    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private RegistryKey<World> affinity$lastWorld = null;

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
