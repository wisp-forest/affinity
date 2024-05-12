package io.wispforest.affinity.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Style.class)
public class StyleMixin {

    @ModifyReturnValue(method = "isObfuscated", at = @At("TAIL"))
    private boolean weObfuscaten(boolean original) {
        if (!MixinHooks.textObfuscation) return original;
        return !original;
    }

}
