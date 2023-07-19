package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "prepareStartRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getPersistentStateManager()Lnet/minecraft/world/PersistentStateManager;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void loadPinChunks(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci, Iterator var5, ServerWorld serverWorld2) {
        serverWorld2.getComponent(AffinityComponents.WORLD_PINS).addAllPins();
    }
}
