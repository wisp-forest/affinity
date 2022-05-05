package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.util.Rarity;

public class ResplendentGemItem extends EnchantedBookItem {

    public ResplendentGemItem() {
        super(AffinityItems.settings(0).rarity(Rarity.UNCOMMON));
    }

}
