package io.wispforest.affinity.compat.emi;

import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.gui.DrawContext;

public class LargeSlotWidget extends AffinitySlotWidget {

    public LargeSlotWidget(EmiIngredient stack, int x, int y) {
        super(stack, x, y);
        this.drawBack(false);
    }

    @Override
    public Bounds getBounds() {
        var bounds = super.getBounds();
        return new Bounds(bounds.x(), bounds.y(), bounds.width() * 2, bounds.height() * 2);
    }

    @Override
    public SlotWidget large(boolean large) {
        return this;
    }

    @Override
    public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
        super.render(draw, mouseX, mouseY, delta);
    }

    @Override
    public void drawStack(DrawContext draw, int mouseX, int mouseY, float delta) {
        BlockStateEmiStack.renderLarge = true;
        getStack().render(draw, this.x + 2, this.y + 2, delta);
        BlockStateEmiStack.renderLarge = false;
    }

    @Override
    public void drawOverlay(DrawContext draw, int mouseX, int mouseY, float delta) {
        if (this.catalyst) {
            EmiRender.renderCatalystIcon(getStack(), draw, this.x, this.y);
        }

        if (shouldDrawSlotHighlight(mouseX, mouseY)) {
            drawSlotHighlight(draw, this.getBounds());
        }
    }
}
