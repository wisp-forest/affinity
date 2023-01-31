package io.wispforest.affinity.client.screen;

import io.wispforest.affinity.misc.screenhandler.OuijaBoardScreenHandler;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.concurrent.ThreadLocalRandom;

public class OuijaBoardScreen extends BaseUIModelHandledScreen<FlowLayout, OuijaBoardScreenHandler> {

    public OuijaBoardScreen(OuijaBoardScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("../src/main/resources/assets/affinity/owo_ui/ouija_board.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(ButtonComponent.class, "curse-button").onPress(button -> {
            this.handler.executeCurse(new OuijaBoardScreenHandler.CurseMessage(ThreadLocalRandom.current().nextInt(3) + 1));
        });
    }
}
