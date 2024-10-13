package io.wispforest.affinity.client;

import io.wispforest.owo.ui.base.BaseOwoTooltipComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.item.ItemStack;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PhantomBundleTooltipComponent extends BaseOwoTooltipComponent<FlowLayout> {
    protected PhantomBundleTooltipComponent(List<ItemStack> stacks) {
        super(() -> {
            var container = Containers.ltrTextFlow(Sizing.fixed(Math.min(stacks.size(), 6) * 16), Sizing.content());
            for (var stack : stacks) {
                container.child(Components.item(stack).showOverlay(true));
            }

            return container;
        });
    }
}
