package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinStates;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public class StyleMixin {

    @Inject(method = "isObfuscated", at = @At("HEAD"), cancellable = true)
    private void weObfuscaten(CallbackInfoReturnable<Boolean> cir) {
        if (!MixinStates.TEXT_OBFUSCATION) return;
        cir.setReturnValue(true);
    }

}
