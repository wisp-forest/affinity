package io.wispforest.affinity.client.screen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import static io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler.*;

public class RitualSocleComposerScreen extends BaseUIModelHandledScreen<FlowLayout, RitualSocleComposerScreenHandler> {

    private ButtonComponent mergeButton, splitButton;

    public RitualSocleComposerScreen(RitualSocleComposerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, Affinity.id("ritual_socle_composer"));
        this.backgroundHeight = 175;

        this.titleY = 69420;
        this.playerInventoryTitleY = 82;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.mergeButton = rootComponent.childById(ButtonComponent.class, "merge-button");
        this.mergeButton.onPress(button -> AffinityNetwork.CHANNEL.clientHandle().send(new ActionRequestPacket(Action.REQUEST_MERGE)));

        this.splitButton = rootComponent.childById(ButtonComponent.class, "split-button");
        this.splitButton.onPress(button -> AffinityNetwork.CHANNEL.clientHandle().send(new ActionRequestPacket(Action.REQUEST_SPLIT)));
    }

    @Override
    protected void handledScreenTick() {
        this.mergeButton.active = canMerge(this.handler.itemAt(ORNAMENT_INPUT_SLOT),
                this.handler.itemAt(BLANK_SOCLE_INPUT_SLOT),
                this.handler.itemAt(SOCLE_SLOT));

        this.splitButton.active = canSplit(this.handler.itemAt(SOCLE_SLOT),
                this.handler.itemAt(ORNAMENT_OUTPUT_SLOT),
                this.handler.itemAt(BLANK_SOCLE_OUTPUT_SLOT));
    }
}
