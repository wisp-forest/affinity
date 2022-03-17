package io.wispforest.affinity.misc.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class ListUtil {

    public static void addItem(DefaultedList<ItemStack> items, ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) continue;
            items.set(i, stack);
            break;
        }
    }

    public static ItemStack getAndRemoveLast(DefaultedList<ItemStack> items) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i).isEmpty()) continue;

            return items.set(i, ItemStack.EMPTY);
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack peekLast(DefaultedList<ItemStack> items) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i).isEmpty()) continue;

            return items.get(i);
        }

        return ItemStack.EMPTY;
    }

    public static int nonEmptyStacks(DefaultedList<ItemStack> items) {
        return (int) items.stream().filter(stack -> !stack.isEmpty()).count();
    }

}
