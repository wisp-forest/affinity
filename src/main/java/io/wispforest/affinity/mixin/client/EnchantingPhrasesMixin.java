package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnchantingPhrases.class)
public class EnchantingPhrasesMixin {

    @ModifyArg(method = "generatePhrase", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/MutableText;fillStyle(Lnet/minecraft/text/Style;)Lnet/minecraft/text/MutableText;"))
    private Style noStyleWhileIlliterate(Style original) {
        if (!MixinHooks.textObfuscation) return original;
        return original.withFont(null);
    }

}
