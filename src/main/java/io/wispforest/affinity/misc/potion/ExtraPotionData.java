package io.wispforest.affinity.misc.potion;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.component.*;
import net.minecraft.item.ItemStack;

import java.util.Set;

public final class ExtraPotionData {
    private static final Set<ComponentType<?>> MARKED_COMPONENTS = new ReferenceOpenHashSet<>();

    private ExtraPotionData() {

    }

    public static void mark(ComponentType<?> type) {
        MARKED_COMPONENTS.add(type);
    }

    public static boolean isExtraData(ComponentType<?> type) {
        return MARKED_COMPONENTS.contains(type);
    }

    public static ComponentChanges copyExtraDataChanges(ItemStack from) {
        var builder = ComponentChanges.builder();

        for (var component : from.getComponents()) {
            if (!isExtraData(component.type())) continue;

            builder.add(component);
        }

        return builder.build();
    }

    public static void copyExtraData(ItemStack from, ItemStack to) {
        to.applyChanges(copyExtraDataChanges(from));
    }

    public static void copyExtraData(ItemStack from, ComponentMapImpl to) {
        to.applyChanges(copyExtraDataChanges(from));
    }
}
