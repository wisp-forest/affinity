package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.WorldPinsComponent;
import io.wispforest.affinity.mixin.access.ServerChunkManagerAccessor;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Shadow @Final private ServerChunkManager chunkManager;

    @Inject(method = "shouldTick(Lnet/minecraft/util/math/ChunkPos;)Z", at = @At("HEAD"), cancellable = true)
    private void worldPinTick(ChunkPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (WorldPinsComponent.shouldTick(((ServerChunkManagerAccessor) this.chunkManager).getTicketManager(), pos))
            cir.setReturnValue(true);
    }
}
