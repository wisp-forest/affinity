package io.wispforest.affinity.client;

import io.wispforest.affinity.item.StaffItem;
import io.wispforest.owo.ui.base.BaseOwoTooltipComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;

@SuppressWarnings("UnstableApiUsage")
public class StaffBundleTooltipComponent extends BaseOwoTooltipComponent<FlowLayout> {
    protected StaffBundleTooltipComponent(StaffItem.BundleTooltipData data) {
        super(() -> Components.list(data.bundleStacks(), flowLayout -> {}, Components::item, false));
    }
}
