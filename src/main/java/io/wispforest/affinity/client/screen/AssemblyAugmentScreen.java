package io.wispforest.affinity.client.screen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.screen.component.AlphaWrapper;
import io.wispforest.affinity.misc.screenhandler.AssemblyAugmentScreenHandler;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class AssemblyAugmentScreen extends BaseUIModelHandledScreen<FlowLayout, AssemblyAugmentScreenHandler> {

    private TextureComponent treetapOverlay;
    private TextureComponent craftingOverlay;
    private TextureComponent progressIndicator;
    private AlphaWrapper<?> progressWrapper;

    private final Slot craftingOutputSlot = this.handler.getSlot(0);
    private final Slot augmentOutputSlot = this.handler.getSlot(this.handler.slots.size() - 1);

    public AssemblyAugmentScreen(AssemblyAugmentScreenHandler handler, PlayerInventory inventory, Text title) {
//        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("../src/main/resources/assets/affinity/owo_ui/assembly_augment.xml"));
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.asset(Affinity.id("assembly_augment")));
        this.titleY = 69420;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.treetapOverlay = rootComponent.childById(TextureComponent.class, "treetap-overlay");
        this.craftingOverlay = rootComponent.childById(TextureComponent.class, "crafting-overlay");
        this.progressIndicator = rootComponent.childById(TextureComponent.class, "progress-indicator");
        this.progressWrapper = rootComponent.childById(AlphaWrapper.class, "progress-alpha");
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        if (!(this.handler.craftingProgress() > 0)) {
            this.progressWrapper.alpha(((float) Math.sin(System.currentTimeMillis() / 500d) + 1f) / 2f);
        } else {
            this.progressWrapper.alpha(1f);
        }

        if (this.augmentOutputSlot.hasStack() || this.handler.matchesAutocraftingRecipe()) {
            this.disableSlot(this.craftingOutputSlot.id);
            this.enableSlot(this.augmentOutputSlot.id);
        } else {
            this.enableSlot(this.craftingOutputSlot.id);
            this.disableSlot(this.augmentOutputSlot.id);
        }
    }

    @Override
    protected void handledScreenTick() {
        float progress = this.handler.craftingProgress();
        this.progressIndicator.visibleArea(PositionedRectangle.of(
                0, 0, (int) (this.progressIndicator.width() * progress), this.progressIndicator.height()
        ));

        float treetapProgress = this.handler.treetapCount() / 5f;
        this.treetapOverlay.visibleArea(PositionedRectangle.of(
                0, 0, this.treetapOverlay.width(), (int) (this.treetapOverlay.height() * treetapProgress)
        ));

        if (this.augmentOutputSlot.hasStack()) {
            this.craftingOverlay.resetVisibleArea();
        } else {
            this.craftingOverlay.visibleArea(PositionedRectangle.of(0, 0, 0, 0));
        }
    }

    public int rootX() {
        return this.x;
    }

    public int rootY() {
        return this.y;
    }

    static {
        UIParsing.registerFactory("affinity.alpha-wrapper", element -> new AlphaWrapper<>(null));
    }
}
