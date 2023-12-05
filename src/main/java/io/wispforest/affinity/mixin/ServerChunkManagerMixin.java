package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.ChunkAethumComponent;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {

    @Shadow
    @Final
    private ServerWorld world;

    @Shadow private boolean spawnAnimals;

    @Shadow private boolean spawnMonsters;

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Info;ZZZ)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void theMobsDoInFactSeemToBeSpawningSomewhatSwiftlyToday(CallbackInfo ci, long l, long m, WorldProperties worldProperties, Profiler profiler, int i, boolean bl2, int j, SpawnHelper.Info info, List<?> list, boolean bl3, Iterator<?> var14, @Coerce Object bruh, WorldChunk worldChunk2, ChunkPos chunkPos) {
        if (!worldChunk2.getComponent(AffinityComponents.CHUNK_AETHUM).isEffectActive(ChunkAethumComponent.INCREASED_NATURAL_SPAWNING)) {
            return;
        }
        SpawnHelper.spawn(this.world, worldChunk2, info, this.spawnAnimals, this.spawnMonsters, bl2);
    }

}

