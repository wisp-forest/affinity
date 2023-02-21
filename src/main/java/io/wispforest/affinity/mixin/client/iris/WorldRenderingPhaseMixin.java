package io.wispforest.affinity.mixin.client.iris;

import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(WorldRenderingPhase.class)
public class WorldRenderingPhaseMixin {

    @Inject(method = "fromTerrainRenderType", at = @At("HEAD"), cancellable = true)
    private static void injectSkyStencil(RenderLayer layer, CallbackInfoReturnable<WorldRenderingPhase> cir) {
        if (layer == SkyCaptureBuffer.SKY_STENCIL_LAYER) {
            cir.setReturnValue(WorldRenderingPhase.NONE);
        }
    }

}
