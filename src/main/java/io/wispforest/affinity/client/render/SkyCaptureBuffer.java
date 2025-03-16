package io.wispforest.affinity.client.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.client.AffinityClient;
import io.wispforest.affinity.misc.quack.AffinityFramebufferExtension;
import io.wispforest.affinity.mixin.access.FramebufferAccessor;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class SkyCaptureBuffer extends RenderLayer {

    private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

    public static final RenderLayer SKY_STENCIL_LAYER = RenderLayer.of("affinity:sky_stencil",
            VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            0x200000,
            MultiPhaseParameters.builder()
                    .program(SOLID_PROGRAM)
                    .target(new Target("affinity:sky_stencil", SkyCaptureBuffer::beginStencilWrite, SkyCaptureBuffer::endStencilWrite))
                    .texture(BLOCK_ATLAS_TEXTURE)
                    .build(true)
    );

    public static final RenderLayer SKY_STENCIL_ENTITY_LAYER = RenderLayer.of("affinity:sky_stencil_entity",
            VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            0x200000,
            MultiPhaseParameters.builder()
                    .program(new ShaderProgram(AffinityClient.SOLID_FROM_FRAMEBUFFER::program))
                    .texture(new TextureBase(
                            () -> {
                                RenderSystem.setShaderTexture(0, SkyCaptureBuffer.skyCapture.getColorAttachment());
                                beginStencilWrite();
                            }, SkyCaptureBuffer::endStencilWrite
                    ))
                    .build(true)
    );

    public static final RenderLayer SKY_IMMEDIATE_LAYER = RenderLayer.of("affinity:sky_immediate",
            VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            0x200000,
            MultiPhaseParameters.builder()
                    .program(new ShaderProgram(AffinityClient.SOLID_FROM_FRAMEBUFFER::program))
                    .target(RenderPhase.MAIN_TARGET)
                    .texture(new TextureBase(() -> RenderSystem.setShaderTexture(0, SkyCaptureBuffer.skyCapture.getColorAttachment()), () -> {}))
                    .build(true)
    );

    private static Framebuffer skyCapture = null;
    private static StencilFramebuffer skyStencil = null;
    private static int lastFramebuffer = 0;
    private static final Vector4i LAST_VIEWPORT = new Vector4i();

    private SkyCaptureBuffer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        throw new UnsupportedOperationException("do not the object");
    }

    public static void init() {
        var window = MinecraftClient.getInstance().getWindow();
        skyCapture = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
        skyStencil = new StencilFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight());
    }

    public static void captureSky(int blockTarget, int viewWidth, int viewHeight) {
        skyCapture.clear(MinecraftClient.IS_SYSTEM_MAC);
        skyStencil.clear(MinecraftClient.IS_SYSTEM_MAC);

        skyCapture.beginWrite(false);
        GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, blockTarget);

        GL30C.glBlitFramebuffer(
                0, 0, MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight(),
                0, 0, skyCapture.textureWidth, skyCapture.textureHeight,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST
        );

        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, blockTarget);
        GlStateManager._viewport(0, 0, viewWidth, viewHeight);
    }

    public static void draw() {
        RenderSystem.backupProjectionMatrix();

        // bind a shader used during terrain rendering
        // to trick iris into binding the proper target for us to steal
        GameRenderer.getRenderTypeSolidProgram().bind();
        var blockTarget = GlStateManager.getBoundFramebuffer();
        GameRenderer.getRenderTypeSolidProgram().unbind();

        skyStencil.beginWrite(true);

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_NOTEQUAL, 0, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

        skyCapture.draw(skyCapture.textureWidth, skyCapture.textureHeight);

        GL11.glDisable(GL11.GL_STENCIL_TEST);

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, blockTarget);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        AffinityClient.DEPTH_MERGE_BLIT_PROGRAM.setupSamplers(skyStencil.getDepthAttachment());
        skyStencil.draw(skyStencil.textureWidth, skyStencil.textureHeight, false);
        RenderSystem.disableBlend();

        skyStencil.clear(MinecraftClient.IS_SYSTEM_MAC);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, blockTarget);

        RenderSystem.restoreProjectionMatrix();
    }

    private static void beginStencilWrite() {
        lastFramebuffer = GlStateManager.getBoundFramebuffer();
        if (lastFramebuffer != MinecraftClient.getInstance().getFramebuffer().fbo) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer viewport = stack.mallocInt(4);
                GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
                LAST_VIEWPORT.get(viewport);
            }
        }

        skyStencil.beginWrite(true);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, MinecraftClient.getInstance().getFramebuffer().fbo);

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glStencilMask(0xFF);
    }

    private static void endStencilWrite() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        if (lastFramebuffer == MinecraftClient.getInstance().getFramebuffer().fbo) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        } else {
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFramebuffer);
            GlStateManager._viewport(LAST_VIEWPORT.x, LAST_VIEWPORT.y, LAST_VIEWPORT.z, LAST_VIEWPORT.w);
        }
    }

    static {
        WindowResizeCallback.EVENT.register((client, window) -> {
            skyCapture.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
            GlStateManager._bindTexture(skyCapture.getColorAttachment());
            GlStateManager._texImage2D(
                    GlConst.GL_TEXTURE_2D, 0, GL32.GL_R11F_G11F_B10F, skyCapture.textureWidth, skyCapture.textureHeight, 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, null
            );

            skyStencil.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    public static boolean isIrisWorldRendering() {
        if (!IRIS_LOADED) return false;
        return Iris.getPipelineManager()
                .getPipeline()
                .map(pipeline -> pipeline.getPhase() != WorldRenderingPhase.NONE || (pipeline instanceof ShaderRenderingPipeline shaderPipeline && shaderPipeline.shouldOverrideShaders()))
                .orElse(false);
    }

    public static class StencilFramebuffer extends SimpleFramebuffer {

        public StencilFramebuffer(int width, int height) {
            super(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
            ((AffinityFramebufferExtension) this).affinity$setBlitProgram(AffinityClient.DEPTH_MERGE_BLIT_PROGRAM::program);
        }

        @Override
        public void initFbo(int width, int height, boolean getError) {
            RenderSystem.assertOnRenderThreadOrInit();

            this.viewportWidth = width;
            this.viewportHeight = height;
            this.textureWidth = width;
            this.textureHeight = height;
            this.fbo = GlStateManager.glGenFramebuffers();
            this.colorAttachment = TextureUtil.generateTextureId();
            if (this.useDepthAttachment) {
                this.depthAttachment = TextureUtil.generateTextureId();
                GlStateManager._bindTexture(this.depthAttachment);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_COMPARE_MODE, 0);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
                GlStateManager._texImage2D(
                        GlConst.GL_TEXTURE_2D, 0, GL30.GL_DEPTH24_STENCIL8, this.textureWidth, this.textureHeight, 0, GL30.GL_DEPTH_COMPONENT, GL30.GL_UNSIGNED_BYTE, null
                );
            }

            ((FramebufferAccessor) this).affinity$setTexFilter(GlConst.GL_NEAREST, true);
            GlStateManager._bindTexture(this.colorAttachment);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
            GlStateManager._texImage2D(
                    GlConst.GL_TEXTURE_2D, 0, GL30.GL_R11F_G11F_B10F, this.textureWidth, this.textureHeight, 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, null
            );
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.fbo);
            GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, this.colorAttachment, 0);
            if (this.useDepthAttachment) {
                GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GlConst.GL_TEXTURE_2D, this.depthAttachment, 0);
            }

            this.checkFramebufferStatus();
            this.clear(getError);
            this.endRead();
        }

        @Override
        public void clear(boolean getError) {
            super.clear(getError);
            this.beginWrite(false);
            GL30.glClear(GL11.GL_STENCIL_BUFFER_BIT);
            this.endWrite();
        }
    }

}
