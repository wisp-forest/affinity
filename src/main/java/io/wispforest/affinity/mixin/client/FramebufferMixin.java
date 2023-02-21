package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.client.AffinityClient;
import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Framebuffer.class)
public class FramebufferMixin {

    @ModifyVariable(method = "drawInternal", at = @At(value = "CONSTANT", args = "stringValue=DiffuseSampler", shift = At.Shift.BEFORE))
    private ShaderProgram iLikeShaders(ShaderProgram value) {
        if (!((Object) this instanceof SkyCaptureBuffer.StencilFramebuffer stencilFramebuffer)) return value;
        return AffinityClient.SKY_BLIT_PROGRAM.setupAndGet();
    }

}
