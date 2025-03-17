package io.wispforest.affinity.mixin.client.iris;

import com.mojang.blaze3d.platform.GlStateManager;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import io.wispforest.affinity.misc.CompatMixin;
import net.irisshaders.iris.pathways.HorizonRenderer;
import net.minecraft.client.gl.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Pseudo
@CompatMixin("iris")
@Mixin(HorizonRenderer.class)
public class HorizonRendererMixin {

    @Inject(method = "renderHorizon", at = @At("TAIL"))
    private void captureShaderSky(Matrix4fc modelView, Matrix4fc projection, ShaderProgram shader, CallbackInfo ci) {
        if (!Affinity.config().theSkyIrisIntegration()) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer viewport = stack.mallocInt(4);

            shader.bind();
            var fb = GlStateManager.getBoundFramebuffer();
            GL11C.glGetIntegerv(GL11C.GL_VIEWPORT, viewport);
            shader.unbind();

            SkyCaptureBuffer.captureSky(fb, viewport.get(2), viewport.get(3));
        }
    }
}
