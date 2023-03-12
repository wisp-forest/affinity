package io.wispforest.affinity.client.screen;

import io.wispforest.affinity.blockentity.impl.ItemTransferNodeBlockEntity;
import io.wispforest.affinity.misc.screenhandler.ItemTransferNodeScreenHandler;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.BlockComponent;
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

import java.util.function.Consumer;

public class ItemTransferNodeScreen extends BaseUIModelHandledScreen<FlowLayout, ItemTransferNodeScreenHandler> {

    private CheckboxComponent ignoreDamageToggle;
    private CheckboxComponent ignoreDataToggle;
    private CheckboxComponent invertFilterToggle;

    public ItemTransferNodeScreen(ItemTransferNodeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("../src/main/resources/assets/affinity/owo_ui/item_transfer_node.xml"));

        this.backgroundHeight += 26;

        this.titleY = 69420;
        this.playerInventoryTitleY = 69420;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.bind(this.handler.filterStack, this::updateNodePreview);

        this.ignoreDamageToggle = rootComponent.childById(CheckboxComponent.class, "ignore-damage-toggle");
        this.ignoreDamageToggle.onChanged(checked -> this.sendFilterState());
        this.bind(this.handler.ignoreDamage, checked -> this.ignoreDamageToggle.checked(checked));

        this.ignoreDataToggle = rootComponent.childById(CheckboxComponent.class, "ignore-data-toggle");
        this.ignoreDataToggle.onChanged(checked -> this.sendFilterState());
        this.bind(this.handler.ignoreData, checked -> this.ignoreDataToggle.checked(checked));

        this.invertFilterToggle = rootComponent.childById(CheckboxComponent.class, "invert-filter-toggle");
        this.invertFilterToggle.onChanged(checked -> this.sendFilterState());
        this.bind(this.handler.invertFilter, checked -> this.invertFilterToggle.checked(checked));
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
            anchor.child(
                    Components.block(AffinityBlocks.ITEM_TRANSFER_NODE.getDefaultState(), nodeNbt).<BlockComponent>configure(block -> {
                        block.sizing(Sizing.fixed(150));
                        block.cursorStyle(CursorStyle.HAND);
                        block.mouseDown().subscribe((mouseX, mouseY, button) -> {
                            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

                            this.handler.updateFilterStack();
                            UISounds.playInteractionSound();

                            return true;
                        });

                        if (!stack.isEmpty()) {
                            var tooltip = ItemComponent.tooltipFromItem(stack, this.client.player, null);
                            tooltip.add(TooltipComponent.of(OrderedText.empty()));
                            tooltip.add(TooltipComponent.of(Text.literal("Click to clear filter").formatted(Formatting.GRAY).asOrderedText()));

                            block.tooltip(tooltip);
                        }
                    })
            );
        });
    }

    private <T> void bind(Observable<T> observable, Consumer<T> function) {
        function.accept(observable.get());
        observable.observe(function);
    }
}
