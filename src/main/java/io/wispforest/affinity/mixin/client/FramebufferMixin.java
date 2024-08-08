package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.misc.quack.AffinityFramebufferExtension;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Supplier;

@Mixin(Framebuffer.class)
public class FramebufferMixin implements AffinityFramebufferExtension {

    @Unique
    private Supplier<ShaderProgram> blitProgram = null;

    @ModifyVariable(method = "drawInternal", at = @At(value = "CONSTANT", args = "stringValue=DiffuseSampler", shift = At.Shift.BEFORE))
    private ShaderProgram iLikeShaders(ShaderProgram value) {
        return this.blitProgram != null ? this.blitProgram.get() : value;
    }

    @Override
    public void affinity$setBlitProgram(Supplier<ShaderProgram> blitProgram) {
        this.blitProgram = blitProgram;
    }

    @Override
    public void affinity$setRenderColor(Color color) {
        if (this.blitProgram != null && this.blitProgram.get().colorModulator != null) {
            this.blitProgram.get().colorModulator.set(color.red(), color.green(), color.blue(), color.alpha());
        } else {
            // TODO this needs a custom shader
//            MinecraftClient.getInstance().gameRenderer.blitScreenProgram.colorModulator.set(color.red(), color.green(), color.blue(), color.alpha());
        }
    }
}
