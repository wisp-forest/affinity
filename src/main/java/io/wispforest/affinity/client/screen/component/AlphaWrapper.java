package io.wispforest.affinity.client.screen.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.util.math.MatrixStack;

public class AlphaWrapper<C extends Component> extends WrappingParentComponent<C> {

    protected float alpha = 1f;

    public AlphaWrapper(C child) {
        super(Sizing.content(), Sizing.content(), child);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        var color = RenderSystem.getShaderColor();
        var previousAlpha = color[3];

        color[3] = alpha * color[3];
        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.childView);
        color[3] = previousAlpha;

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
