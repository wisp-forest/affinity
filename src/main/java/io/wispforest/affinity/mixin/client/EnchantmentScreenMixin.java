package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentScreen.class)
public class EnchantmentScreenMixin {

    @Unique
    private boolean restoreTextObfuscation = false;

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWrapped(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/StringVisitable;IIII)V"))
    private void disableObfuscation(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!MixinHooks.textObfuscation) return;

        this.restoreTextObfuscation = true;
        MixinHooks.textObfuscation = false;
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWrapped(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/StringVisitable;IIII)V", shift = At.Shift.AFTER))
    private void reeanbleObfuscation(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!this.restoreTextObfuscation) return;

        this.restoreTextObfuscation = false;
        MixinHooks.textObfuscation = true;
    }
}
