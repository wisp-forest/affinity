package io.wispforest.affinity.blockentity.template;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemSocleBlockEntity {
    @NotNull ItemStack getItem();
}
