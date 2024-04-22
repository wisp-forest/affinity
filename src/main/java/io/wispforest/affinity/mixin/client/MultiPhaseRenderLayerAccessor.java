package io.wispforest.affinity.mixin.client;

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderLayer.MultiPhase.class)
public interface MultiPhaseRenderLayerAccessor {
    @Invoker("getPhases")
    RenderLayer.MultiPhaseParameters affinity$getPhases();
}
