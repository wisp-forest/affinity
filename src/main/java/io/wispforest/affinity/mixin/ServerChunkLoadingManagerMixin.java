package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.WorldPinsComponent;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {
    @Shadow
    @Final
    private ServerChunkLoadingManager.TicketManager ticketManager;

    @Inject(method = "shouldTick", at = @At("HEAD"), cancellable = true)
    private void enableTicksFromWorldPin(ChunkPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (WorldPinsComponent.shouldTick(ticketManager, pos)) {
            cir.setReturnValue(true);
        }
    }
}
