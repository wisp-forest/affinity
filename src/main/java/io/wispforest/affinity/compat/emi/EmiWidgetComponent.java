package io.wispforest.affinity.compat.emi;

import dev.emi.emi.api.widget.Widget;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;

public class EmiWidgetComponent extends BaseComponent {

    private final WidgetMaker widgetMaker;
    private Widget widget;

    public EmiWidgetComponent(WidgetMaker widgetMaker) {
        this.widgetMaker = widgetMaker;
        this.widget = this.widgetMaker.instantiateAt(0, 0);

        this.mouseEnter().subscribe(() -> {
            this.focusHandler().focus(this, FocusSource.KEYBOARD_CYCLE);
        });

        this.mouseLeave().subscribe(() -> {
            this.focusHandler().focus(null, null);
        });
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        this.tooltip(this.widget.getTooltip(mouseX, mouseY));
        this.widget.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void drawFocusHighlight(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {}

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.widget.getBounds().width();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.widget.getBounds().height();
    }

    private void refreshWidget() {
        this.widget = this.widgetMaker.instantiateAt(this.x, this.y);
    }

    @Override
    public void mount(ParentComponent parent, int x, int y) {
        super.mount(parent, x, y);
        this.refreshWidget();
    }

    @Override
    public void updateX(int x) {
        super.updateX(x);
        this.refreshWidget();
    }

    @Override
    public void updateY(int y) {
        super.updateY(y);
        this.refreshWidget();
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        return this.widget.mouseClicked((int) mouseX, (int) mouseY, button) | super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        return this.widget.keyPressed(keyCode, scanCode, modifiers) | super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    public interface WidgetMaker {
        Widget instantiateAt(int x, int y);
    }
}
