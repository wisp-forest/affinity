package io.wispforest.affinity.client.screen;

import io.wispforest.affinity.misc.screenhandler.OuijaBoardScreenHandler;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OuijaBoardScreen extends BaseUIModelHandledScreen<FlowLayout, OuijaBoardScreenHandler> {

    private LabelComponent curseLabel1 = null;
    private LabelComponent curseLabel2 = null;
    private LabelComponent curseLabel3 = null;

    public OuijaBoardScreen(OuijaBoardScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("../src/main/resources/assets/affinity/owo_ui/ouija_board.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(ButtonComponent.class, "curse-button-1").onPress(button -> {
            this.handler.executeCurse(new OuijaBoardScreenHandler.CurseMessage(1));
        });

        rootComponent.childById(ButtonComponent.class, "curse-button-2").onPress(button -> {
            this.handler.executeCurse(new OuijaBoardScreenHandler.CurseMessage(2));
        });

        rootComponent.childById(ButtonComponent.class, "curse-button-3").onPress(button -> {
            this.handler.executeCurse(new OuijaBoardScreenHandler.CurseMessage(3));
        });

        this.curseLabel1 = rootComponent.childById(LabelComponent.class, "curse-label-1");
        this.curseLabel2 = rootComponent.childById(LabelComponent.class, "curse-label-2");
        this.curseLabel3 = rootComponent.childById(LabelComponent.class, "curse-label-3");
    }

    public void updateCurses() {
        this.curseLabel1.text(this.handler.currentCurses()[0].getName(1).copy().styled(style -> style.withFont(new Identifier("minecraft", "alt"))));
        this.curseLabel1.parent().tooltip(this.handler.currentCurses()[0].getName(1));

        this.curseLabel2.text(this.handler.currentCurses()[1].getName(1).copy().styled(style -> style.withFont(new Identifier("minecraft", "alt"))));
        this.curseLabel2.parent().tooltip(this.handler.currentCurses()[1].getName(1));

        this.curseLabel3.text(this.handler.currentCurses()[2].getName(1).copy().styled(style -> style.withFont(new Identifier("minecraft", "alt"))));
        this.curseLabel3.parent().tooltip(this.handler.currentCurses()[2].getName(1));
    }
}
