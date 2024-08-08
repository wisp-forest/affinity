package io.wispforest.affinity.mixin.client.sodium;

import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import io.wispforest.affinity.misc.CompatMixin;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Pseudo
@CompatMixin("sodium")
@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin {

    @Shadow private RenderSectionManager renderSectionManager;

    @Inject(method = "drawChunkLayer", at = @At("TAIL"))
    private void injectSkyStencil(RenderLayer renderLayer, ChunkRenderMatrices matrices, double x, double y, double z, CallbackInfo ci) {
        if (renderLayer != SkyCaptureBuffer.SKY_STENCIL_LAYER) return;
        this.renderSectionManager.renderLayer(matrices, DefaultMaterials.forRenderLayer(SkyCaptureBuffer.SKY_STENCIL_LAYER).pass, x, y, z);
    }

}
