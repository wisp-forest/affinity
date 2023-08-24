package io.wispforest.affinity.mixin.client.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(TerrainRenderPass.class)
public interface TerrainRenderPassAccessor {
    @Accessor("layer")
    RenderLayer affinity$getLayer();
}
