package io.wispforest.affinity.mixin.client.sodium;

import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import io.wispforest.affinity.misc.CompatMixin;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Pseudo
@CompatMixin("sodium")
@Mixin(value = DefaultMaterials.class, remap = false)
public abstract class DefaultMaterialsMixin {

    @Unique
    private static final Material SKY_STENCIL = new Material(
            Arrays.stream(DefaultTerrainRenderPasses.ALL).filter(pass -> ((TerrainRenderPassAccessor) pass).affinity$getLayer() == SkyCaptureBuffer.SKY_STENCIL_LAYER).findFirst().get(),
            AlphaCutoffParameter.ONE_TENTH,
            false
    );

    @Inject(method = "forRenderLayer", at = @At("HEAD"), cancellable = true)
    private static void injectSkyStencil(RenderLayer layer, CallbackInfoReturnable<Material> cir) {
        if (layer == SkyCaptureBuffer.SKY_STENCIL_LAYER) {
            cir.setReturnValue(SKY_STENCIL);
        }
    }
}
