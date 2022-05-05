package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.client.render.AbsoluteEnchantmentGlintHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilderStorage.class)
public class BufferBuilderStorageMixin {

    @Inject(method = "method_22999", at = @At("TAIL"))
    private void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> builderStorage, CallbackInfo callbackInfo) {
        AbsoluteEnchantmentGlintHandler.assignBuffers(builderStorage);
    }

}
