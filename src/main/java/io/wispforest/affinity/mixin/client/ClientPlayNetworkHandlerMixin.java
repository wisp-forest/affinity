package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.misc.CelestialZoomer;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow private ClientWorld world;

    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyArg(method = "onWorldTimeUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;setTimeOfDay(J)V"))
    private long itIsAlwaysNightyNight(long serverTimeOfDay) {
        CelestialZoomer.serverTimeOfDay = serverTimeOfDay;

        if (isInWispForest()) {
            CelestialZoomer.enableOffset((Math.abs(serverTimeOfDay) / 24000) * -24000 - 18000);
        } else if (CelestialZoomer.offsetEnabled()) {
            CelestialZoomer.disableOffset();
        }

        return CelestialZoomer.offsetEnabled() ? world.getTimeOfDay() : serverTimeOfDay;
    }

    @Unique
    private boolean isInWispForest() {
        return Objects.equals(world.getRegistryManager().get(Registry.BIOME_KEY).
                getKey(world.getBiome(client.player.getBlockPos())).orElse(null), AffinityWorldgen.WISP_FOREST_KEY);
    }
}
