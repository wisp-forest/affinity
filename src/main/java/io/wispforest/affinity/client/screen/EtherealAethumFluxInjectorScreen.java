package io.wispforest.affinity.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.EtherealAethumFluxInjectorBlockEntity;
import io.wispforest.affinity.client.render.blockentity.LinkRenderer;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.BlockComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Delta;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EtherealAethumFluxInjectorScreen extends BaseUIModelScreen<FlowLayout> {

    private final BlockPos injectorPos;
    private final List<GlobalPos> globalNodes, privateNodes;
    private final Map<GlobalPos, Text> nodeNames;
    private GlobalPos currentSource;

    private boolean showingPrivateNodes = true;

    private RadialLayout nodeList;

    public EtherealAethumFluxInjectorScreen(
            BlockPos injectorPos,
            List<GlobalPos> globalNodes,
            List<GlobalPos> privateNodes,
            Map<GlobalPos, Text> nodeNames,
            @Nullable GlobalPos currentSource
    ) {
        super(FlowLayout.class, Affinity.id("ethereal_aethum_flux_injector"));
        this.injectorPos = injectorPos;
        this.globalNodes = globalNodes;
        this.privateNodes = privateNodes;
        this.nodeNames = nodeNames;
        this.currentSource = currentSource;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.component(ButtonComponent.class, "private-button").onPress($ -> {
            this.refreshScreenState(true);
        });
        this.component(ButtonComponent.class, "public-button").onPress($ -> {
            this.refreshScreenState(false);
        });

        this.nodeList = new RadialLayout(Sizing.fill(100), Sizing.fill(100));
        this.component(FlowLayout.class, "node-list-anchor").child(nodeList);

        this.rebuildNodeList();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        var candidates = new ArrayList<Component>();
        this.component(FlowLayout.class, "node-list-anchor").collectDescendants(candidates);

        var selectedNodeComponent = candidates.stream()
                .filter(component -> component instanceof InjectorSourceComponent source && source.selected)
                .findAny()
                .orElse(null);

        if (selectedNodeComponent != null) {
            context.draw();
            int x1 = this.width / 2, y1 = this.height / 2;
            int x2 = selectedNodeComponent.x() + selectedNodeComponent.width() / 2, y2 = selectedNodeComponent.y() + selectedNodeComponent.height() / 2;

            var offset = new Vector2d(x2 - x1, y2 - y1);
            context.push()
                    .translate(x1, y1, 0)
                    .multiply(RotationAxis.NEGATIVE_Z.rotation((float) offset.angle(new Vector2d(1, 0))))
                    .scale((float) offset.length(), 1, 1);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderSystem.setShaderColor(Affinity.AETHUM_FLUX_COLOR.red(), Affinity.AETHUM_FLUX_COLOR.green(), Affinity.AETHUM_FLUX_COLOR.blue(), 1f);
            context.drawTexture(LinkRenderer.LINK_TEXTURE_ID, 0, 0, 1, 3, 0, 0, 8, 8, 8, 8);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            context.pop();
        }
    }

    private void refreshScreenState(boolean showPrivateNodes) {
        this.showingPrivateNodes = showPrivateNodes;

        this.rebuildNodeList();

        this.component(Component.class, "button-selection-marker").remove();
        var button = this.component(ButtonComponent.class, this.showingPrivateNodes ? "private-button" : "public-button");
        ((FlowLayout) button.parent()).child(0, this.model.expandTemplate(Component.class, "button-selection-marker", Map.of()));
    }

    private void rebuildNodeList() {
        var nodes = this.showingPrivateNodes ? this.privateNodes : this.globalNodes;
        this.nodeList.<RadialLayout>configure(layout -> {
            layout.clearChildren();
            for (var nodePos : nodes) {
                var nodeItem = Containers.verticalFlow(Sizing.content(), Sizing.content());
                nodeItem.horizontalAlignment(HorizontalAlignment.CENTER);

                nodeItem.child(new InjectorSourceComponent().<InjectorSourceComponent>configure(component -> {
                    component.sizing(Sizing.fixed(48));

                    var tooltip = new ArrayList<Text>();
                    tooltip.add(Text.translatable(
                            "text.affinity.ethereal_aethum_flux_injector.node_location_tooltip",
                            nodePos.getPos().getX() + " " + nodePos.getPos().getY() + " " + nodePos.getPos().getZ(),
                            Text.translatable(Util.createTranslationKey("dimension", nodePos.getDimension().getValue()))
                    ));

                    if (nodePos.equals(this.currentSource)) {
                        component.selected = true;
                        component.scale = 1f;

                        tooltip.add(Text.translatable("text.affinity.ethereal_aethum_flux_injector.linked_node_tooltip"));
                    } else {
                        tooltip.add(Text.translatable("text.affinity.ethereal_aethum_flux_injector.unlinked_node_tooltip"));

                        component.cursorStyle(CursorStyle.HAND);
                        component.mouseDown().subscribe((mouseX, mouseY, button) -> {
                            this.currentSource = nodePos;

                            UISounds.playInteractionSound();
                            AffinityNetwork.CHANNEL.clientHandle().send(new EtherealAethumFluxInjectorBlockEntity.SetInjectorNodePacket(
                                    this.injectorPos,
                                    nodePos
                            ));

                            component.parent().queue(this::rebuildNodeList);
                            return true;
                        });
                    }

                    component.tooltip(tooltip);
                }));

                if (this.nodeNames.containsKey(nodePos)) {
                    nodeItem.child(Components.label(this.nodeNames.get(nodePos))
                            .color(Color.ofFormatting(Formatting.GRAY))
                            .shadow(true)
                            .maxWidth(60)
                            .horizontalTextAlignment(HorizontalAlignment.CENTER)
                            .margins(Insets.top(-5)));
                }

                layout.child(nodeItem);
            }
        });
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class RadialLayout extends BaseParentComponent {

        protected final List<Component> children = new ArrayList<>();
        protected final List<Component> childrenView = Collections.unmodifiableList(this.children);

        public RadialLayout(Sizing horizontalSizing, Sizing verticalSizing) {
            super(horizontalSizing, verticalSizing);
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.children);
        }

        @Override
        public void layout(Size space) {
            double angleStep = Math.PI / (this.children.size()) * 2;
            int centerX = this.x + this.width / 2 - 24, centerY = this.y + this.height / 2 - 24;
            int radius = (int) Math.min(centerX * .85, centerY * .85);

            for (int i = 0; i < this.children.size(); i++) {
                var child = this.children.get(i);
                var angle = angleStep * i - Math.PI / 2;

                child.inflate(space);
                child.mount(this, centerX + (int) (radius * Math.cos(angle)), centerY + (int) (radius * Math.sin(angle)));
            }
        }

        public RadialLayout child(Component child) {
            this.children.add(child);
            this.updateLayout();
            return this;
        }

        public RadialLayout clearChildren() {
            for (var child : this.children) {
                child.dismount(DismountReason.REMOVED);
            }

            this.children.clear();
            this.updateLayout();

            return this;
        }

        @Override
        public RadialLayout removeChild(Component child) {
            if (this.children.remove(child)) {
                child.dismount(DismountReason.REMOVED);
                this.updateLayout();
            }

            return this;
        }

        @Override
        public List<Component> children() {
            return this.childrenView;
        }
    }

    private static class InjectorSourceComponent extends BlockComponent {

        private static final BlockState DISPLAY_STATE = AffinityBlocks.ETHEREAL_AETHUM_FLUX_NODE.getDefaultState();

        private float scale = .85f;
        private boolean selected = false;

        public InjectorSourceComponent() {
            super(DISPLAY_STATE, Util.make(() -> {
                var be = ((BlockEntityProvider) DISPLAY_STATE.getBlock()).createBlockEntity(MinecraftClient.getInstance().player.getBlockPos(), DISPLAY_STATE);
                prepareBlockEntity(DISPLAY_STATE, be, null);
                return be;
            }));
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            this.scale += Delta.compute(this.scale, this.selected || this.hovered ? 1f : .85f, delta * .5f);

            context.push()
                    .translate(this.x + this.width / 2f, this.y + this.height / 2f, 0)
                    .scale(this.scale, this.scale, this.scale)
                    .translate(-this.x - this.width / 2f, -this.y - this.height / 2f, 0);
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            context.pop();
        }

        @Override
        public boolean onMouseDown(double mouseX, double mouseY, int button) {
            var result = super.onMouseDown(mouseX, mouseY, button);

            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                UISounds.playInteractionSound();

                this.selected = true;
                this.root().forEachDescendant(component -> {
                    if (component != this && component instanceof InjectorSourceComponent sourceComponent) {
                        sourceComponent.selected = false;
                    }
                });

                return true;
            }

            return result;
        }
    }
}
