package io.wispforest.affinity.client.render.program;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;

public class DepthMergeBlitProgram extends GlProgram {

    public DepthMergeBlitProgram() {
        super(Affinity.id("blit_merge_depth"), VertexFormats.BLIT_SCREEN);
    }

    public void setupSamplers(int secondaryDepthSampler) {
        this.backingProgram.addSampler("MainDepthSampler", MinecraftClient.getInstance().getFramebuffer().getDepthAttachment());
        this.backingProgram.addSampler("SecondaryDepthSampler", secondaryDepthSampler);
    }

    public ShaderProgram program() {
        return this.backingProgram;
    }
}
