package io.wispforest.affinity.mixin.client.iris;

import com.mojang.blaze3d.platform.GlStateManager;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import io.wispforest.affinity.misc.CompatMixin;
import net.irisshaders.iris.pathways.HorizonRenderer;
import net.minecraft.client.gl.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@CompatMixin("iris")
@Mixin(HorizonRenderer.class)
public class HorizonRendererMixin {

    @Inject(method = "renderHorizon", at = @At("TAIL"))
    private void captureShaderSky(Matrix4fc modelView, Matrix4fc projection, ShaderProgram shader, CallbackInfo ci) {
        if (!Affinity.config().theSkyIrisIntegration()) return;

        shader.bind();
        var fb = GlStateManager.getBoundFramebuffer();
        shader.unbind();

        SkyCaptureBuffer.captureSky(fb);
    }
}
