package io.wispforest.affinity.item;

import io.wispforest.affinity.registries.AffinityBlocks;
import io.wispforest.affinity.registries.AffinityItems;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class AffinityItemGroup extends OwoItemGroup {

    public AffinityItemGroup(Identifier id) {
        super(id);
    }

    @Override
    protected void setup() {
        this.addTab(Icon.of(AffinityItems.EMERALD_WAND_OF_IRIDESCENCE), "main", ItemGroupTab.EMPTY);
        this.addTab(Icon.of(AffinityBlocks.AZALEA_LOG), "nature", ItemGroupTab.EMPTY);

        this.addButton(ItemGroupButton.github("https://github.com/glisco03/affinity"));
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(AffinityBlocks.SUNDIAL);
    }
}
