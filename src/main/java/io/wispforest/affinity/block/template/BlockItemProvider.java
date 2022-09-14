package io.wispforest.affinity.block.template;

import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface BlockItemProvider {
    Item createBlockItem(Block block, OwoItemSettings settings);
}
