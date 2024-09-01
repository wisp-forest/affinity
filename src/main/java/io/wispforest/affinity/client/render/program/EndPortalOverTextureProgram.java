package io.wispforest.affinity.client.render.program;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;

public class EndPortalOverTextureProgram extends GlProgram {
    public EndPortalOverTextureProgram() {
        super(Affinity.id("end_portal_over_texture"), VertexFormats.POSITION_TEXTURE);
    }

    public ShaderProgram program() {
        return this.backingProgram;
    }
}
