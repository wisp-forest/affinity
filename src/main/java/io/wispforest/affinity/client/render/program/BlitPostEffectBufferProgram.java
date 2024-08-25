package io.wispforest.affinity.client.render.program;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;

public class BlitPostEffectBufferProgram extends GlProgram {
    public BlitPostEffectBufferProgram() {
        super(Affinity.id("blit_post_effect_buffer"), VertexFormats.BLIT_SCREEN);
    }

    public ShaderProgram program() {
        return this.backingProgram;
    }
}
