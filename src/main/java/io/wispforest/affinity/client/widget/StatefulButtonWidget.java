package io.wispforest.affinity.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class StatefulButtonWidget extends TexturedButtonWidget {

    private final Identifier texture;
    private final int u, v, hoveredVOffset;

    public StatefulButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, PressAction pressAction) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, pressAction);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.hoveredVOffset = hoveredVOffset;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);
        int i = this.v;
        if (this.isHovered() && this.active) {
            i += this.hoveredVOffset;
        } else if (!this.active) {
            i -= this.hoveredVOffset;
        }

        RenderSystem.enableDepthTest();
        drawTexture(matrices, this.x, this.y, (float) this.u, (float) i, this.width, this.height, 256, 256);
        if (this.hovered) {
            this.renderTooltip(matrices, mouseX, mouseY);
        }
    }
}
