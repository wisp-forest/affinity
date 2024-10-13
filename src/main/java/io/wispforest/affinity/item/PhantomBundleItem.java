package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PhantomBundleItem extends Item {

    public static final ComponentType<StacksComponent> STACKS = Affinity.component(
        "phantom_bundle_stacks",
        MinecraftEndecs.ITEM_STACK.listOf().xmap(StacksComponent::new, StacksComponent::stacks)
    );

    public PhantomBundleItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (!slot.hasStack() || clickType != ClickType.RIGHT) return false;

        var phantomStacks = new ArrayList<>(stack.getOrDefault(STACKS, StacksComponent.DEFAULT).stacks);
        var refStack = ItemOps.singleCopy(slot.getStack());

        if (phantomStacks.stream().noneMatch(it -> ItemStack.areItemsAndComponentsEqual(it, refStack))) {
            phantomStacks.add(refStack);
        } else {
            phantomStacks.removeIf(it -> ItemStack.areItemsAndComponentsEqual(it, refStack));
        }

        if (phantomStacks.isEmpty()) {
            stack.remove(STACKS);
        } else {
            stack.set(STACKS, new StacksComponent(phantomStacks));
        }

        return true;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        var component = stack.get(STACKS);
        if (component == null || component.stacks.isEmpty()) return Optional.empty();

        return Optional.of(new StacksTooltipData(component.stacks));
    }

    public record StacksTooltipData(List<ItemStack> stacks) implements TooltipData {}

    public record StacksComponent(List<ItemStack> stacks) {
        public static final StacksComponent DEFAULT = new StacksComponent(List.of());

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof StacksComponent other && ItemStack.stacksEqual(other.stacks, this.stacks);
        }

        @Override
        public int hashCode() {
            return ItemStack.listHashCode(this.stacks);
        }
    }
}
