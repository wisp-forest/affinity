package io.wispforest.affinity.client.render.program;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;

public class SolidFromFramebufferProgram extends GlProgram {

    public SolidFromFramebufferProgram() {
        super(Affinity.id("solid_from_fb"), VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
    }

    public ShaderProgram program() {
        return this.backingProgram;
    }
}
