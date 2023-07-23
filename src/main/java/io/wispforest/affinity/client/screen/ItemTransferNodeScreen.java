package io.wispforest.affinity.client.screen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.ItemTransferNodeBlockEntity;
import io.wispforest.affinity.misc.screenhandler.ItemTransferNodeScreenHandler;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class ItemTransferNodeScreen extends BaseUIModelHandledScreen<FlowLayout, ItemTransferNodeScreenHandler> {

    private CheckboxComponent ignoreDamageToggle;
    private CheckboxComponent ignoreDataToggle;
    private CheckboxComponent invertFilterToggle;

    public ItemTransferNodeScreen(ItemTransferNodeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, Affinity.id("item_transfer_node"));

        this.backgroundHeight += 26;

        this.titleY = 69420;
        this.playerInventoryTitleY = 69420;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.bind(this.handler.filterStack, this::updateNodePreview);

        this.ignoreDamageToggle = this.bindToggle("ignore-damage-toggle", this.handler.ignoreDamage);
        this.ignoreDataToggle = this.bindToggle("ignore-data-toggle", this.handler.ignoreData);
        this.invertFilterToggle = this.bindToggle("invert-filter-toggle", this.handler.invertFilter);

        rootComponent.childById(FlowLayout.class, "node-click-area").<FlowLayout>configure(clickArea -> {
            clickArea.cursorStyle(CursorStyle.HAND);
            clickArea.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

                this.handler.updateFilterStack();
                UISounds.playInteractionSound();

                return true;
            });
        });
    }

    private CheckboxComponent bindToggle(String id, Observable<Boolean> target) {
        var toggle = this.uiAdapter.rootComponent.childById(CheckboxComponent.class, id);
        toggle.onChanged(checked -> this.sendFilterState());

        this.bind(target, toggle::checked);

        return toggle;
    }

    private void sendFilterState() {
        this.handler.updateFilterConfiguration(
                this.ignoreDamageToggle.isChecked(),
                this.ignoreDataToggle.isChecked(),
                this.invertFilterToggle.isChecked()
        );
    }

    private void updateNodePreview(ItemStack stack) {
        var nodeNbt = new NbtCompound();
        nodeNbt.put(ItemTransferNodeBlockEntity.FILTER_STACK_KEY, stack);

        this.uiAdapter.rootComponent.childById(FlowLayout.class, "node-preview-anchor").<FlowLayout>configure(anchor -> {
            anchor.clearChildren();
            anchor.child(Components.block(AffinityBlocks.ITEM_TRANSFER_NODE.getDefaultState(), nodeNbt).sizing(Sizing.fixed(150)));
        });

        if (!stack.isEmpty()) {
            var tooltip = ItemComponent.tooltipFromItem(stack, this.client.player, null);
            tooltip.add(TooltipComponent.of(OrderedText.empty()));
            tooltip.add(TooltipComponent.of(Text.literal("Click to clear filter").formatted(Formatting.GRAY).asOrderedText()));

            this.uiAdapter.rootComponent.childById(FlowLayout.class, "node-click-area").tooltip(tooltip);
        } else {
            this.uiAdapter.rootComponent.childById(FlowLayout.class, "node-click-area").tooltip((List<TooltipComponent>) null);
        }
    }

    private <T> void bind(Observable<T> observable, Consumer<T> function) {
        function.accept(observable.get());
        observable.observe(function);
    }
}
