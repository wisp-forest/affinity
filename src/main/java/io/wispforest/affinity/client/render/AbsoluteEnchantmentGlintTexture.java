package io.wispforest.affinity.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.util.MathUtil;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.math.MathHelper;

public class AbsoluteEnchantmentGlintTexture extends RenderPhase.Texture {

    private final float[] colors;

    public AbsoluteEnchantmentGlintTexture(int hue) {
        super(Affinity.id("textures/glint.png"), true, false);
        this.colors = MathUtil.splitRGBToFloats(MathHelper.hsvToRgb(hue / 360f, .5f, .2f));
    }

    @Override
    public void startDrawing() {
        super.startDrawing();
        RenderSystem.setShaderColor(colors[0] * 5, colors[1] * 5, colors[2] * 5, 1);
    }

    @Override
    public void endDrawing() {
        super.endDrawing();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}
