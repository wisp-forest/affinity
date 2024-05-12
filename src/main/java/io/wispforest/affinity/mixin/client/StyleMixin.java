package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public class StyleMixin {

    @Inject(method = "isObfuscated", at = @At("HEAD"), cancellable = true)
    private void weObfuscaten(CallbackInfoReturnable<Boolean> cir) {
        if (!MixinHooks.textObfuscation) return;
        cir.setReturnValue(!cir.getReturnValueZ());
    }

}
