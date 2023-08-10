package io.wispforest.affinity.client.render;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;

public class DownsampleProgram extends GlProgram {

    private Framebuffer inputBuffer;
    private GlUniform inputResolution;
    private GlUniform guiScale;

    public DownsampleProgram() {
        super(Affinity.id("downsample"), VertexFormats.POSITION);
    }

    public void prepare(Framebuffer framebuffer) {
        this.inputBuffer = framebuffer;
    }

    @Override
    public void use() {
        if (this.inputResolution != null) {
            this.inputResolution.set((float) this.inputBuffer.textureWidth, (float) this.inputBuffer.textureHeight);
        }

        if (this.guiScale != null) {
            this.guiScale.set((float) MinecraftClient.getInstance().getWindow().getScaleFactor());
        }

        this.backingProgram.addSampler("InputSampler", this.inputBuffer.getColorAttachment());

        super.use();
    }

    @Override
    protected void setup() {
        super.setup();
        this.inputResolution = this.findUniform("InputResolution");
        this.guiScale = this.findUniform("GuiScale");
    }
}
