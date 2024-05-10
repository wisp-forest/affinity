package io.wispforest.affinity.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.StaffItem;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.SpriteComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectStaffFromBundleScreen extends BaseUIModelScreen<FlowLayout> {

    private final Hand hand;
    private final ItemStack stack;

    private final List<StackLayout> selectionItems = new ArrayList<>();
    private int currentStaffIdx = -1;

    public SelectStaffFromBundleScreen(Hand hand, ItemStack stack) {
        super(FlowLayout.class, Affinity.id("select_staff_from_bundle"));
        this.hand = hand;
        this.stack = stack;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(ItemComponent.class, "bundle-preview").stack(this.stack);

        var bundle = this.stack.get(StaffItem.BUNDLED_STAFFS);
        var row = rootComponent.childById(FlowLayout.class, "bundled-staffs-row");
        for (int i = 0; i < bundle.size(); i++) {
            var staffStack = bundle.get(i);
            var staffIdx = i;

            var button = this.model.expandTemplate(StackLayout.class, "bundled-staff", Map.of());
            button.childById(ItemComponent.class, "stack").stack(staffStack);

            button.childById(ItemComponent.class, "stack").mouseEnter().subscribe(() -> this.select(staffIdx));
            button.childById(ItemComponent.class, "stack").mouseLeave().subscribe(() -> this.select(-1));
            button.childById(ItemComponent.class, "stack").mouseDown().subscribe((mouseX, mouseY, mouseButton) -> {
                if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
                submitSelection(staffIdx);

                return true;
            });

            row.child(button);
            this.selectionItems.add(button);
        }
    }

    private void submitSelection(int staffIdx) {
        UISounds.playInteractionSound();

        AffinityNetwork.CHANNEL.clientHandle().send(new StaffItem.SelectStaffFromBundlePacket(this.hand, staffIdx));
        this.invalid = true;
    }

    private void select(int idx) {
        if (this.currentStaffIdx != -1) {
            var button = this.selectionItems.get(this.currentStaffIdx);
            button.children().get(button.children().size() - 1).remove();
        }

        this.currentStaffIdx = idx;
        var bundlePreview = this.component(ItemComponent.class, "bundle-preview").stack(this.stack);

        if (this.currentStaffIdx != -1) {
            bundlePreview.stack(StaffItem.selectStaffFromBundle(this.stack, this.currentStaffIdx));
            this.selectionItems.get(this.currentStaffIdx).child(Components.texture(Affinity.id("textures/gui/sprites/staff_bundle_selection.png"), 0, 0, 26, 26, 26, 26).cursorStyle(CursorStyle.HAND));
        } else {
            bundlePreview.stack(this.stack);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (super.mouseScrolled(mouseX, mouseY, amount)) return true;

        if (amount == 0) return false;

        var newIdx = this.wrap(this.currentStaffIdx + (int) amount);
        this.select(this.currentStaffIdx == -1 && amount < 0 ? newIdx + 1 : newIdx);

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;

        if (this.currentStaffIdx == -1 || !(keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) return false;
        this.submitSelection(this.currentStaffIdx);

        return true;
    }

    private int wrap(int idx) {
        while (idx < 0) idx += this.selectionItems.size();
        while (idx >= this.selectionItems.size()) idx -= this.selectionItems.size();

        return idx;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static class AffinitySpriteComponent extends SpriteComponent {

        private boolean blend = false;

        public AffinitySpriteComponent(Sprite sprite) {
            super(sprite);
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            if (this.blend) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }

            super.draw(context, mouseX, mouseY, partialTicks, delta);

            if (this.blend) {
                RenderSystem.disableBlend();
            }
        }

        public AffinitySpriteComponent blend(boolean blend) {
            this.blend = blend;
            return this;
        }

        public boolean blend() {
            return this.blend;
        }

        @Override
        public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
            super.parseProperties(model, element, children);
            UIParsing.apply(children, "blend", UIParsing::parseBool, this::blend);
        }

        public static AffinitySpriteComponent parse(Element element) {
            UIParsing.expectAttributes(element, "atlas", "sprite");

            var atlas = UIParsing.parseIdentifier(element.getAttributeNode("atlas"));
            var sprite = UIParsing.parseIdentifier(element.getAttributeNode("sprite"));

            return new AffinitySpriteComponent(new SpriteIdentifier(atlas, sprite).getSprite());
        }
    }

    static {
        UIParsing.registerFactory("affinity.sprite", AffinitySpriteComponent::parse);
    }
}
