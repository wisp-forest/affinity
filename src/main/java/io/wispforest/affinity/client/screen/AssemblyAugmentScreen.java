package io.wispforest.affinity.client.screen;

import io.wispforest.affinity.misc.screenhandler.AssemblyAugmentScreenHandler;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class AssemblyAugmentScreen extends BaseUIModelHandledScreen<FlowLayout, AssemblyAugmentScreenHandler> {

    private LabelComponent treetapCount;

    public AssemblyAugmentScreen(AssemblyAugmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("../src/main/resources/assets/affinity/owo_ui/assembly_augment.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.treetapCount = rootComponent.childById(LabelComponent.class, "treetap-count");
    }

    @Override
    protected void handledScreenTick() {
        this.treetapCount.text(Text.literal(String.valueOf(this.handler.treetapCount())));
    }
}
