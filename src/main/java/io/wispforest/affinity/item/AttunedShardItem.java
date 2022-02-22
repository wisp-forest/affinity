package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import net.minecraft.item.Item;

public class AttunedShardItem extends Item {

    private final AttunedShardTier tier;

    public AttunedShardItem(AttunedShardTier tier) {
        super(AffinityItems.settings(0));
        this.tier = tier;
    }

    public AttunedShardTier tier() {
        return tier;
    }
}
