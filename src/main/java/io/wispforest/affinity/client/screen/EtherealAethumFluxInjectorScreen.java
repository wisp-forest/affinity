package io.wispforest.affinity.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.EtherealAethumFluxInjectorBlockEntity;
import io.wispforest.affinity.client.render.blockentity.LinkRenderer;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.BlockComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
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
    private final List<BlockPos> globalNodes, privateNodes;
    private final Map<BlockPos, Text> nodeNames;
    private BlockPos currentSource;

    private boolean showingPrivateNodes = true;

    private RadialLayout nodeList;

    public EtherealAethumFluxInjectorScreen(
            BlockPos injectorPos,
            List<BlockPos> globalNodes,
            List<BlockPos> privateNodes,
            Map<BlockPos, Text> nodeNames,
            @Nullable BlockPos currentSource
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

        this.nodeList = new RadialLayout(Sizing.fill(), Sizing.fill());
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
                layout.child(new InjectorSourceComponent().<InjectorSourceComponent>configure(component -> {
                    component.sizing(Sizing.fixed(48));
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

                    var tooltip = new ArrayList<Text>();
                    tooltip.add(this.nodeNames.containsKey(nodePos) ? this.nodeNames.get(nodePos) : Text.literal(nodePos.getX() + " " + nodePos.getY() + " " + nodePos.getZ()));

                    if (nodePos.equals(this.currentSource)) {
                        component.selected = true;
                        component.scale = 1f;

                        tooltip.add(TextOps.withColor("[§⌘§] §Linked", Formatting.DARK_GRAY.getColorValue(), Affinity.AETHUM_FLUX_COLOR.rgb(), Formatting.DARK_GRAY.getColorValue(), Formatting.GRAY.getColorValue()));
                    } else {
                        tooltip.add(TextOps.withFormatting("[§+§] §Click to link", Formatting.DARK_GRAY, Formatting.GREEN, Formatting.DARK_GRAY, Formatting.GRAY));
                    }

                    component.tooltip(tooltip);
                }));
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

            this.cursorStyle(CursorStyle.HAND);
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
