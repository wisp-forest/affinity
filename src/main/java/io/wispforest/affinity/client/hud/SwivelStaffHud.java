package io.wispforest.affinity.client.hud;

import com.google.common.base.Suppliers;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.SwivelStaffItem;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;

public class SwivelStaffHud {

    public static final Identifier COMPONENT_ID = Affinity.id("swivel_staff");

    public static void initialize() {
        var component = Suppliers.memoize(() -> {
            return Containers.verticalFlow(Sizing.content(), Sizing.content())
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .positioning(Positioning.relative(50, 100))
                    .margins(Insets.bottom(64));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            //noinspection DataFlowIssue
            var stack = client.player.getMainHandStack();
            if (stack.getItem() instanceof SwivelStaffItem) {
                if (!Hud.hasComponent(COMPONENT_ID)) Hud.add(COMPONENT_ID, component);

                var potentialComponent = Hud.getComponent(COMPONENT_ID);
                if (!(potentialComponent instanceof FlowLayout container)) return;

                var targetState = client.crosshairTarget instanceof BlockHitResult blockHit
                        ? client.world.getBlockState(blockHit.getBlockPos())
                        : null;

                container.<FlowLayout>configure(layout -> {
                    layout.clearChildren();

                    if (targetState != null) {
                        var swivelProperties = SwivelStaffItem.swivelProperties(targetState);
                        if (swivelProperties.isEmpty()) return;

                        var selectedPropName = stack.get(SwivelStaffItem.SELECTED_PROPERTY);
                        //noinspection rawtypes
                        Property selectedProp = swivelProperties.stream()
                                .map(Pair::getLeft)
                                .filter(property -> property.getName().equals(selectedPropName))
                                .findFirst()
                                .map(property -> (Property) property)
                                .orElse(swivelProperties.get(0).getLeft());

                        for (var property : swivelProperties) {
                            var text = Text.translatable(property.getRight());
                            if (property.getLeft() == selectedProp) {
                                text.formatted(Formatting.GOLD);
                            }

                            layout.child(Components.label(text).shadow(true).margins(Insets.bottom(3)));
                        }
                    }
                });
            } else {
                Hud.remove(COMPONENT_ID);
            }
        });
    }
}
