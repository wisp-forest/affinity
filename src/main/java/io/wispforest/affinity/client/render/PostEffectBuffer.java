package io.wispforest.affinity.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.misc.quack.AffinityFramebufferExtension;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

public class PostEffectBuffer {

    private Framebuffer framebuffer = null;
    private int prevBuffer = 0;

    public void clear() {
        this.ensureInitialized();

        int previousBuffer = GlStateManager.getBoundFramebuffer();
        this.framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousBuffer);
    }

    public void beginWrite(boolean clear, int blitFromMain) {
        this.ensureInitialized();

        this.prevBuffer = GlStateManager.getBoundFramebuffer();
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

        this.framebuffer.beginWrite(false);
    }

    public void end() {
        GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, this.prevBuffer);
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

    public Framebuffer buffer() {
        this.ensureInitialized();
        return this.framebuffer;
    }

    private void ensureInitialized() {
        if (this.framebuffer != null) return;

        this.framebuffer = new SimpleFramebuffer(MinecraftClient.getInstance().getFramebuffer().textureWidth, MinecraftClient.getInstance().getFramebuffer().textureHeight, true, MinecraftClient.IS_SYSTEM_MAC);
        this.framebuffer.setClearColor(0, 0, 0, 0);

        WindowResizeCallback.EVENT.register((client, window) -> this.framebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC));
    }

}
