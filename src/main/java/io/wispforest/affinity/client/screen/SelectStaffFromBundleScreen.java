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
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.Map;

public class SelectStaffFromBundleScreen extends BaseUIModelScreen<FlowLayout> {

    private final Hand hand;
    private final ItemStack stack;

    public SelectStaffFromBundleScreen(Hand hand, ItemStack stack) {
        super(FlowLayout.class, Affinity.id("select_staff_from_bundle"));
        this.hand = hand;
        this.stack = stack;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var bundlePreview = rootComponent.childById(ItemComponent.class, "bundle-preview").stack(this.stack);

        var bundle = this.stack.get(StaffItem.BUNDLED_STAFFS);
        var row = rootComponent.childById(FlowLayout.class, "bundled-staffs-row");
        for (int i = 0; i < bundle.size(); i++) {
            var staffStack = bundle.get(i);
            var staffIdx = i;

            var button = this.model.expandTemplate(StackLayout.class, "bundled-staff", Map.of());
            button.childById(ItemComponent.class, "stack").stack(staffStack);

            button.childById(ItemComponent.class, "stack").mouseEnter().subscribe(() -> {
                bundlePreview.stack(StaffItem.selectStaffFromBundle(this.stack, staffIdx));
                button.child(Components.sprite(this.client.getGuiAtlasManager().getSprite(Affinity.id("staff_bundle_selection"))).cursorStyle(CursorStyle.HAND));
            });
            button.childById(ItemComponent.class, "stack").mouseLeave().subscribe(() -> {
                bundlePreview.stack(this.stack);
                button.children().get(button.children().size() - 1).remove();
            });
            button.childById(ItemComponent.class, "stack").mouseDown().subscribe((mouseX, mouseY, mouseButton) -> {
                if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
                UISounds.playInteractionSound();

                AffinityNetwork.CHANNEL.clientHandle().send(new StaffItem.SelectStaffFromBundlePacket(this.hand, staffIdx));
                this.close();

                return true;
            });

            row.child(button);
        }
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

            if (atlas.equals(new Identifier("textures/atlas/gui.png"))) {
                return new AffinitySpriteComponent(MinecraftClient.getInstance().getGuiAtlasManager().getSprite(sprite));
            } else {
                return new AffinitySpriteComponent(new SpriteIdentifier(atlas, sprite).getSprite());
            }
        }
    }

    static {
        UIParsing.registerFactory(Affinity.id("sprite"), AffinitySpriteComponent::parse);
    }
}
