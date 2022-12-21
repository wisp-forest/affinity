package io.wispforest.affinity.client.screen.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class AlphaWrapper<C extends Component> extends WrappingParentComponent<C> {

    protected float alpha = 1f;

    public AlphaWrapper(C child) {
        super(Sizing.content(), Sizing.content(), child);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);

        var color = new float[4];
        System.arraycopy(RenderSystem.getShaderColor(), 0, color, 0, 4);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(color[0], color[1], color[2], alpha * color[3]);
        this.drawChildren(matrices, mouseX, mouseY, partialTicks, delta, List.of(this.child));
        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);

        RenderSystem.disableBlend();
    }

    public AlphaWrapper<C> alpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public float alpha() {
        return alpha;
    }
}
