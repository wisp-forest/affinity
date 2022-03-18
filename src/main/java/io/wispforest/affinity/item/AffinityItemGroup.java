package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class AffinityItemGroup extends OwoItemGroup {

    public AffinityItemGroup(Identifier id) {
        super(id);
    }

    @Override
    protected void setup() {
        this.addTab(Icon.of(AffinityItems.EMERALD_WAND_OF_IRIDESCENCE), "main", null);
        this.addTab(Icon.of(AffinityBlocks.AZALEA_LOG), "nature", null);

        this.addButton(ItemGroupButton.github("https://github.com/gliscowo/affinity"));
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(AffinityBlocks.SUNDIAL);
    }
}
