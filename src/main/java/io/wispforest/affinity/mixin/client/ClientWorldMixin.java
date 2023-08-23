package io.wispforest.affinity.mixin.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.wispforest.affinity.misc.CelestialZoomer;
import io.wispforest.affinity.misc.quack.AffinityClientWorldExtension;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World implements AffinityClientWorldExtension {

    @Unique
    private final Multimap<Long, Runnable> sectionUpdateListeners = HashMultimap.create();

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Shadow
    public abstract void setTimeOfDay(long timeOfDay);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void resetZoomer(ClientPlayNetworkHandler netHandler, ClientWorld.Properties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, int loadDistance, int simulationDistance, Supplier<Profiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        CelestialZoomer.reset();
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MutableWorldProperties;getGameRules()Lnet/minecraft/world/GameRules;"), cancellable = true)
    private void zooooom(CallbackInfo ci) {
        CelestialZoomer.lastWorldTime = this.getTimeOfDay();
        if (!CelestialZoomer.offsetEnabled()) return;

        this.setTimeOfDay(CelestialZoomer.getZoomedTime(this));
        ci.cancel();
    }

    @Override
    public void affinity$addChunkSectionListener(ChunkSectionPos pos, Runnable listener) {
        this.sectionUpdateListeners.put(pos.asLong(), listener);
    }

    @Inject(method = "updateListeners", at = @At("HEAD"))
    private void invokeListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        final var updatePos = ChunkSectionPos.asLong(
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getY()),
                ChunkSectionPos.getSectionCoord(pos.getZ())
        );

        this.sectionUpdateListeners.get(updatePos).forEach(Runnable::run);
        this.sectionUpdateListeners.removeAll(updatePos);
    }
}
