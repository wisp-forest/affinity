package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.util.CelestialZoomer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Shadow
    public abstract void setTimeOfDay(long timeOfDay);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void resetZoomer(ClientPlayNetworkHandler netHandler, ClientWorld.Properties properties, RegistryKey registryRef, DimensionType dimensionType, int loadDistance, int simulationDistance, Supplier profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        CelestialZoomer.reset();
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MutableWorldProperties;getGameRules()Lnet/minecraft/world/GameRules;"), cancellable = true)
    private void zooooom(CallbackInfo ci) {
        CelestialZoomer.lastWorldTime = getTimeOfDay();
        if (!CelestialZoomer.offsetEnabled()) return;

        setTimeOfDay(CelestialZoomer.getZoomedTime(this));
        ci.cancel();
    }

}
