package io.wispforest.affinity.item;

import io.wispforest.affinity.endec.BuiltInEndecs;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PhantomBundleItem extends Item {

    public static final KeyedEndec<List<ItemStack>> STACKS = BuiltInEndecs.ITEM_STACK.listOf().keyed("filter_stacks", ArrayList::new);

    public PhantomBundleItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (!slot.hasStack() || clickType != ClickType.RIGHT) return false;

        var phantomStacks = new ArrayList<>(stack.get(STACKS));
        var refStack = ItemOps.singleCopy(slot.getStack());

        if (phantomStacks.stream().noneMatch(it -> ItemStack.areEqual(it, refStack))) {
            phantomStacks.add(refStack);
        } else {
            phantomStacks.removeIf(it -> ItemStack.areEqual(it, refStack));
        }

        if (phantomStacks.isEmpty()) {
            stack.delete(STACKS);
        } else {
            stack.put(STACKS, phantomStacks);
        }

        return true;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        var stacks = stack.get(STACKS);
        if (stacks == null || stacks.isEmpty()) return Optional.empty();

        return Optional.of(new StacksTooltipData(stacks));
    }

    public record StacksTooltipData(List<ItemStack> stacks) implements TooltipData {}
}
