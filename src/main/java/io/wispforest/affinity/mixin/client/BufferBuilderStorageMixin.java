package io.wispforest.affinity.mixin.client;

import com.google.common.collect.ImmutableList;
import io.wispforest.affinity.client.render.AbsoluteEnchantmentGlintHandler;
import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilderStorage.class)
public class BufferBuilderStorageMixin {

    @Shadow
    private static void assignBufferBuilder(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> builderStorage, RenderLayer layer) {}

    @Inject(method = "method_54639", at = @At("TAIL"))
    private void insertAffinityLayers(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> builderStorage, CallbackInfo callbackInfo) {
        AbsoluteEnchantmentGlintHandler.setupCallbacks(
                builderStorage::remove,
                renderLayer -> assignBufferBuilder(builderStorage, renderLayer)
        );
    }

    static {
        RenderLayerAccessor.setBlockLayers(ImmutableList.<RenderLayer>builder().addAll(RenderLayer.getBlockLayers()).add(SkyCaptureBuffer.SKY_STENCIL_LAYER).build());
    }
}
