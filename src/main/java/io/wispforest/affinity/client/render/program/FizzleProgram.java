package io.wispforest.affinity.client.render.program;

import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class FizzleProgram extends GlProgram {
    public FizzleProgram(Identifier id) {
        super(id, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
    }

    public ShaderProgram program() {
        return this.backingProgram;
    }
}
