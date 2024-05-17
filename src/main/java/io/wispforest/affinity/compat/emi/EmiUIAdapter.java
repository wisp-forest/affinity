package io.wispforest.affinity.compat.emi;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

import java.util.function.BiFunction;

public class EmiUIAdapter<T extends ParentComponent> extends Widget {

    public final OwoUIAdapter<T> adapter;

    public EmiUIAdapter(Bounds bounds, BiFunction<Sizing, Sizing, T> rootComponentMaker) {
        this.adapter = OwoUIAdapter.createWithoutScreen(bounds.x(), bounds.y(), bounds.width(), bounds.height(), rootComponentMaker);
        this.adapter.inspectorZOffset = 900;

        if (MinecraftClient.getInstance().currentScreen != null) {
            ScreenEvents.remove(MinecraftClient.getInstance().currentScreen).register(screen -> this.adapter.dispose());
        }
    }

    public void prepare() {
        this.adapter.inflateAndMount();
    }

    public T rootComponent() {
        return this.adapter.rootComponent;
    }

    public EmiWidgetComponent wrap(EmiWidgetComponent.WidgetMaker widgetMaker) {
        return new EmiWidgetComponent(widgetMaker);
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(0, 0, this.adapter.width(), this.adapter.height());
    }

    @Override
    public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
        this.adapter.render(draw, mouseX, mouseY, delta);
        draw.draw();

        Screen.getTooltipFromItem(MinecraftClient.getInstance(), ItemStack.EMPTY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return this.adapter.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.adapter.keyPressed(keyCode, scanCode, modifiers);
    }
}
