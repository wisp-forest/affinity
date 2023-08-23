package io.wispforest.affinity.misc.quack;

import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.gl.ShaderProgram;

import java.util.function.Supplier;

public interface AffinityFramebufferExtension {
    void affinity$setBlitProgram(Supplier<ShaderProgram> blitProgram);
    void affinity$setRenderColor(Color color);
}
