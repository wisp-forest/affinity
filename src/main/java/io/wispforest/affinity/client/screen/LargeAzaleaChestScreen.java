package io.wispforest.affinity.client.screen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.screenhandler.LargeAzaleaChestScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LargeAzaleaChestScreen extends HandledScreen<LargeAzaleaChestScreenHandler> {
    private static final Identifier TEXTURE = Affinity.id("textures/gui/large_azalea_chest.png");

    public LargeAzaleaChestScreen(LargeAzaleaChestScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundHeight = 276;
        this.backgroundWidth = 194;

        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.playerInventoryTitleX += 9;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int baseX = (this.width - this.backgroundWidth) / 2;
        int baseY = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, baseX, baseY, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 512);
    }
}
