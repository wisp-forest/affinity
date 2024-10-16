package io.wispforest.affinity.mixin.client.sodium;

import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import io.wispforest.affinity.misc.CompatMixin;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@CompatMixin("sodium")
@Mixin(value = DefaultTerrainRenderPasses.class, remap = false)
public class DefaultTerrainRenderPassesMixin {

    @Mutable
    @Shadow
    @Final
    public static TerrainRenderPass[] ALL;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void injectSkyStencilPass(CallbackInfo ci) {
        var newAll = new TerrainRenderPass[ALL.length + 1];
        System.arraycopy(ALL, 0, newAll, 0, ALL.length);

        newAll[ALL.length] = new TerrainRenderPass(SkyCaptureBuffer.SKY_STENCIL_LAYER, false, false);
        ALL = newAll;
    }

}
