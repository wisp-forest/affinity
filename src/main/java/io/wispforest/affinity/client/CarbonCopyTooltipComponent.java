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
import net.minecraft.item.ItemStack;

public class CarbonCopyTooltipComponent implements TooltipComponent {

    private final StackLayout root;

    public CarbonCopyTooltipComponent(CarbonCopyItem.TooltipData data) {
        this.root = Containers.stack(Sizing.content(), Sizing.content());
        this.root.horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER);

        this.root.child(Components.texture(Affinity.id("textures/gui/carbon_copy_tooltip.png"), 0, 0, 128, 64, 128, 64));

        int height, width = 1;
        outer:
        for (height = 1; height <= 3; height++) {
            for (width = 1; width <= 3; width++) {
                if (!data.recipe().fits(width, height)) continue;
                break outer;
            }
        }

        var grid = Containers.grid(Sizing.content(), Sizing.content(), 3, 3);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x < width && y < height) {
                    var displayStacks = data.recipe().getIngredients().get(y * width + x).getMatchingStacks();
                    grid.child(Components.item(displayStacks[(int) (System.currentTimeMillis() / 1000 % displayStacks.length)]).showOverlay(true).margins(Insets.of(1)), y, x);
                } else {
                    grid.child(Components.item(ItemStack.EMPTY).margins(Insets.of(1)), y, x);
                }
            }
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
