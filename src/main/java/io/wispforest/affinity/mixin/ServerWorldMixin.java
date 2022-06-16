package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.WorldPinsComponent;
import io.wispforest.affinity.mixin.access.ServerChunkManagerAccessor;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.dimension.DimensionType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    @Shadow @Final private ServerChunkManager chunkManager;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "shouldTick(Lnet/minecraft/util/math/ChunkPos;)Z", at = @At("HEAD"), cancellable = true)
    private void worldPinTick(ChunkPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (WorldPinsComponent.shouldTick(((ServerChunkManagerAccessor) this.chunkManager).getTicketManager(), pos)) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "tickWeather", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;rainGradientPrev:F", opcode = Opcodes.GETFIELD))
    private float disableRainGradientSending(ServerWorld instance) {
        return rainGradient;
    }

    @Redirect(method = "tickWeather", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;thunderGradientPrev:F", opcode = Opcodes.GETFIELD))
    private float disableThunderGradientSending(ServerWorld instance) {
        return thunderGradient;
    }

    @ModifyVariable(method = "tickWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToDimension(Lnet/minecraft/network/Packet;Lnet/minecraft/util/registry/RegistryKey;)V", ordinal = 1, shift = At.Shift.BY, by = 2))
    private boolean disableRainingSending(boolean old) {
        return isRaining();
    }

    @ModifyArg(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;hasRain(Lnet/minecraft/util/math/BlockPos;)Z"))
    private BlockPos makeSunshineMonolithsStopThunder(BlockPos pos) {
        var chunk = getWorldChunk(pos);

        if (chunk instanceof EmptyChunk) return pos;

        var component = AffinityComponents.LOCAL_WEATHER.get(chunk);

        if (component.hasMonolith()) {
            return new BlockPos(0, -255, 0);
        }

        return pos;
    }
}
