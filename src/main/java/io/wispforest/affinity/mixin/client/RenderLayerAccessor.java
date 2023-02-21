package io.wispforest.affinity.mixin.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderLayer.class)
public interface RenderLayerAccessor {

    @Mutable
    @Accessor("BLOCK_LAYERS")
    static void setBlockLayers(ImmutableList<RenderLayer> layers) {
        throw new UnsupportedOperationException();
    }

}
