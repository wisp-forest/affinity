package io.wispforest.affinity.block.template;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface BlockItemProvider {
    Item createBlockItem(Block block, Item.Settings settings);
}
