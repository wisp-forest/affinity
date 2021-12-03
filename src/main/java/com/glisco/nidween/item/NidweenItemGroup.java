package com.glisco.nidween.item;

import com.glisco.nidween.registries.NidweenBlocks;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class NidweenItemGroup extends OwoItemGroup {

    public NidweenItemGroup(Identifier id) {
        super(id);
    }

    @Override
    protected void setup() {

    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(NidweenBlocks.SUNDIAL);
    }
}
