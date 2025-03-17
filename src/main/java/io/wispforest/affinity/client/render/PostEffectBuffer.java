package io.wispforest.affinity.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.client.AffinityClient;
import io.wispforest.affinity.misc.quack.AffinityFramebufferExtension;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.function.Supplier;

public class PostEffectBuffer {

    private Framebuffer framebuffer = null;
    private int prevBuffer = 0;
    private final Vector4i prevViewport = new Vector4i();
    private int textureFilter = -1;

    public void clear() {
        this.ensureInitialized();

        int previousBuffer = GlStateManager.getBoundFramebuffer();
        this.framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousBuffer);
    }

    public void beginWrite(boolean clear, int blitFromMain) {
        this.ensureInitialized();

        this.prevBuffer = GlStateManager.getBoundFramebuffer();
        if (this.prevBuffer != MinecraftClient.getInstance().getFramebuffer().fbo) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer size = stack.mallocInt(4);
                GL11.glGetIntegerv(GL11.GL_VIEWPORT, size);
                this.prevViewport.get(size);
            }
        }
        if (clear) this.framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);

        if (blitFromMain != 0) {
            GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, this.prevBuffer);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.framebuffer.fbo);
            GL30.glBlitFramebuffer(
                    0, 0,
                    this.framebuffer.textureWidth, this.framebuffer.textureHeight,
                    0, 0,
                    this.framebuffer.textureWidth, this.framebuffer.textureHeight,
                    blitFromMain, GL11.GL_NEAREST
            );
        }

        this.framebuffer.beginWrite(true);
    }

    public void endWrite() {
        if (this.prevBuffer == MinecraftClient.getInstance().getFramebuffer().fbo) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        } else {
            GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, this.prevBuffer);
            GlStateManager._viewport(this.prevViewport.x, this.prevViewport.y, this.prevViewport.z, this.prevViewport.w);
        }
    }

    public void draw(boolean blend) {
        if (blend) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }

        RenderSystem.backupProjectionMatrix();
        this.framebuffer.draw(this.framebuffer.textureWidth, this.framebuffer.textureHeight, !blend);
        RenderSystem.restoreProjectionMatrix();
    }

    public void draw(Color color) {
        ((AffinityFramebufferExtension) this.buffer()).affinity$setRenderColor(color);
        this.draw(true);
        ((AffinityFramebufferExtension) this.buffer()).affinity$setRenderColor(Color.WHITE);
    }

    public void setBlitProgram(Supplier<ShaderProgram> program) {
        ((AffinityFramebufferExtension) this.buffer()).affinity$setBlitProgram(program);
    }

    public void setTextureFilter(int textureFilter) {
        this.textureFilter = textureFilter;
    }

    public Framebuffer buffer() {
        this.ensureInitialized();
        return this.framebuffer;
    }

    private void ensureInitialized() {
        if (this.framebuffer != null) return;

        this.framebuffer = new SimpleFramebuffer(MinecraftClient.getInstance().getFramebuffer().textureWidth, MinecraftClient.getInstance().getFramebuffer().textureHeight, true, MinecraftClient.IS_SYSTEM_MAC);
        this.framebuffer.setClearColor(0, 0, 0, 0);

        ((AffinityFramebufferExtension) this.framebuffer).affinity$setBlitProgram(AffinityClient.BLIT_POST_EFFECT_BUFFER::program);

        WindowResizeCallback.EVENT.register((client, window) -> {
            this.framebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
            if (this.textureFilter != -1) {
                this.framebuffer.setTexFilter(this.textureFilter);
            }
        });
    }

}
