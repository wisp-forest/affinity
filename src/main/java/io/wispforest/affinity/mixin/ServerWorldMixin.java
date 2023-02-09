package io.wispforest.affinity.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.WorldPinsComponent;
import io.wispforest.affinity.mixin.access.ServerChunkManagerAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    @Shadow
    @Final
    private ServerChunkManager chunkManager;

    private static final BlockState AFFINITY$MOCK_STATE = new BlockState(Blocks.AIR, ImmutableMap.of(), MapCodec.unit(null));
    private static final TagKey<Block> AFFINITY$NO_RANDOM_TICKS = TagKey.of(RegistryKeys.BLOCK, Affinity.id("no_random_ticks_in_dying_chunks"));

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @ModifyVariable(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;hasRandomTicks()Z", shift = At.Shift.BEFORE))
    private BlockState cancelRandomTicksInDeadChunks(BlockState state, WorldChunk chunk, int randomTickSpeed) {
        if (!state.isIn(AFFINITY$NO_RANDOM_TICKS)) return state;

        var component = AffinityComponents.CHUNK_AETHUM.get(chunk);
        if (component.getAethum() > 60) return state;

        return AFFINITY$MOCK_STATE;
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

    @ModifyVariable(method = "tickWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToDimension(Lnet/minecraft/network/Packet;Lnet/minecraft/registry/RegistryKey;)V", ordinal = 1, shift = At.Shift.BY, by = 2))
    private boolean disableRainingSending(boolean old) {
        return this.isRaining();
    }

    @ModifyArg(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;hasRain(Lnet/minecraft/util/math/BlockPos;)Z"))
    private BlockPos makeSunshineMonolithsStopThunder(BlockPos pos) {
        var chunk = this.getWorldChunk(pos);

        if (chunk instanceof EmptyChunk) return pos;

        var component = AffinityComponents.LOCAL_WEATHER.get(chunk);

        if (component.hasMonolith()) {
            return new BlockPos(0, -255, 0);
        }

        return pos;
    }
}
