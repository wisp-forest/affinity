package io.wispforest.affinity.client;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.CarbonCopyItem;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class CarbonCopyTooltipComponent implements TooltipComponent {

    private final StackLayout root;

    public CarbonCopyTooltipComponent(CarbonCopyItem.TooltipData data) {
        this.root = Containers.stack(Sizing.content(), Sizing.content());
        this.root.horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER);

        this.root.child(Components.texture(Affinity.id("textures/gui/carbon_copy_tooltip.png"), 0, 0, 128, 64, 128, 64));

        var grid = Containers.grid(Sizing.content(), Sizing.content(), 3, 3);
        for (int i = 0; i < data.inputMatrix().size(); i++) {
            var displayStacks = data.inputMatrix().get(i).getMatchingStacks();
            grid.child(Components.item(displayStacks[(int) (System.currentTimeMillis() / 1000 % displayStacks.length)]).showOverlay(true).margins(Insets.of(1)), i / 3, i % 3);
        }

        this.root.child(grid.margins(Insets.left(5)));
        this.root.child(Components.item(data.result()).showOverlay(true).positioning(Positioning.absolute(106, 23)));

        this.root.inflate(Size.of(1000, 1000));
        this.root.mount(null, 0, 0);
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer) {
        this.root.moveTo(x, y);
        this.root.draw(matrices, 0, 0, 0, 0);
    }

    @Override
    public int getHeight() {
        return this.root.height();
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.root.width();
    }
}
