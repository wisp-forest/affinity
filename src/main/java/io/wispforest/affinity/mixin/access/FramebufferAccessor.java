package io.wispforest.affinity.mixin.access;

import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Framebuffer.class)
public interface FramebufferAccessor {
    @Invoker("setTexFilter")
    void affinity$setTexFilter(int texFilter, boolean force);
}
