package io.wispforest.affinity.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.widget.StatefulButtonWidget;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.affinity.network.AffinityNetwork;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RitualSocleComposerScreen extends HandledScreen<RitualSocleComposerScreenHandler> {

    public final Identifier TEXTURE = Affinity.id("textures/gui/ritual_socle_composer.png");

    private StatefulButtonWidget mergeButton, splitButton;

    public RitualSocleComposerScreen(RitualSocleComposerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 175;
        this.titleY = 5;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        this.playerInventoryTitleY = 82;

        this.mergeButton = new StatefulButtonWidget(this.x + 101, this.y + 20, 27, 13, 176, 13, 13, TEXTURE, button -> {
            AffinityNetwork.CHANNEL.clientHandle().send(new RitualSocleComposerScreenHandler.ActionRequestPacket(
                    RitualSocleComposerScreenHandler.Action.REQUEST_MERGE));
        });


        this.splitButton = new StatefulButtonWidget(this.x + 101, this.y + 63, 27, 13, 203, 13, 13, TEXTURE, button -> {
            AffinityNetwork.CHANNEL.clientHandle().send(new RitualSocleComposerScreenHandler.ActionRequestPacket(
                    RitualSocleComposerScreenHandler.Action.REQUEST_SPLIT));
        });

        this.addDrawableChild(mergeButton);
        this.addDrawableChild(splitButton);
    }

    @Override
    protected void handledScreenTick() {
        this.mergeButton.active = RitualSocleComposerScreenHandler.canMerge(this.handler.itemAt(RitualSocleComposerScreenHandler.ORNAMENT_INPUT_SLOT),
                this.handler.itemAt(RitualSocleComposerScreenHandler.BLANK_SOCLE_INPUT_SLOT),
                this.handler.itemAt(RitualSocleComposerScreenHandler.SOCLE_SLOT));

        this.splitButton.active = RitualSocleComposerScreenHandler.canSplit(this.handler.itemAt(RitualSocleComposerScreenHandler.SOCLE_SLOT),
                this.handler.itemAt(RitualSocleComposerScreenHandler.ORNAMENT_OUTPUT_SLOT),
                this.handler.itemAt(RitualSocleComposerScreenHandler.BLANK_SOCLE_OUTPUT_SLOT));
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matrices, this.x, this.y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        for (Element element : this.children()) {
            if (!(element instanceof Drawable drawable)) continue;
            drawable.render(matrices, mouseX, mouseY, delta);
        }
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
