package io.wispforest.affinity.mixin.client.iris;

import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import io.wispforest.affinity.misc.CompatMixin;
import io.wispforest.affinity.mixin.client.sodium.TerrainRenderPassAccessor;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Pseudo
@CompatMixin("iris")
@Mixin(SodiumPrograms.class)
public class SodiumProgramsMixin {

    @Unique
    private static final TerrainRenderPass SKY_PASS = Arrays.stream(DefaultTerrainRenderPasses.ALL).filter(pass -> ((TerrainRenderPassAccessor) pass).affinity$getLayer() == SkyCaptureBuffer.SKY_STENCIL_LAYER).findFirst().get();

    @Inject(method = "mapTerrainRenderPass", at = @At("HEAD"), cancellable = true, remap = false)
    private void fixSky(TerrainRenderPass pass, CallbackInfoReturnable<SodiumPrograms.Pass> cir) {
        if (pass != SKY_PASS) return;
        cir.setReturnValue(ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? SodiumPrograms.Pass.SHADOW : SodiumPrograms.Pass.TERRAIN);
    }

}
