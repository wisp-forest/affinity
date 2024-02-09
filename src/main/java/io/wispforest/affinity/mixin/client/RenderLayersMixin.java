package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.block.impl.TheSkyBlock;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public class RenderLayersMixin {

    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private static void injectSkyLayers(BlockState state, CallbackInfoReturnable<RenderLayer> cir) {
        if (!state.isOf(AffinityBlocks.THE_SKY) || state.get(TheSkyBlock.ENABLED)) return;
        cir.setReturnValue(RenderLayer.getCutout());
    }

}
