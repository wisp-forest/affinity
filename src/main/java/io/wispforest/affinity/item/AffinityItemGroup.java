package io.wispforest.affinity.item;

import io.wispforest.affinity.registries.AffinityBlocks;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class AffinityItemGroup extends OwoItemGroup {

    public AffinityItemGroup(Identifier id) {
        super(id);
    }

    @Override
    protected void setup() {

    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(AffinityBlocks.SUNDIAL);
    }
}
