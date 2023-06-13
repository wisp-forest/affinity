package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.client.render.blockentity.AethumFluxNodeBlockEntityRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void disableLinkRendering(DrawContext context, float tickDelta, CallbackInfo ci) {
        AethumFluxNodeBlockEntityRenderer.enableLinkRendering = false;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void enableLinkRendering(DrawContext context, float tickDelta, CallbackInfo ci) {
        AethumFluxNodeBlockEntityRenderer.enableLinkRendering = true;
    }
}
