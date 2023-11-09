package io.wispforest.affinity.client.render.program;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;

public class FizzleProgram extends GlProgram {
    public FizzleProgram() {
        super(Affinity.id("fizzle"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
    }

    public ShaderProgram program() {
        return this.backingProgram;
    }
}
