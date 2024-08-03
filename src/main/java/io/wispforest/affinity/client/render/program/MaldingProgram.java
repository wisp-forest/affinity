package io.wispforest.affinity.client.render.program;

import com.google.gson.internal.UnsafeAllocator;
import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;

// A program that does absolutely nothing.
// Used to help porting.
public class MaldingProgram extends GlProgram {
    private MaldingProgram() {
        super(null, null);

        throw new RuntimeException("bruh");
    }

    public static MaldingProgram create() {
        try {
            return UnsafeAllocator.INSTANCE.newInstance(MaldingProgram.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ShaderProgram program() {
        return GameRenderer.getRenderTypeSolidProgram();
    }

    public void setupSamplers(int secondaryDepthSampler) { }

    public void prepare(Framebuffer framebuffer) { }

    @Override
    public void use() { }
}
