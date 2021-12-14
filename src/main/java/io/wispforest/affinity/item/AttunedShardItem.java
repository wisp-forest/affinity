package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.shards.AttunedShardTier;
import net.minecraft.item.Item;

public class AttunedShardItem extends Item {

    private final AttunedShardTier tier;

    public AttunedShardItem(AttunedShardTier tier) {
        super(new Settings().group(Affinity.AFFINITY_GROUP));
        this.tier = tier;
    }

    public AttunedShardTier tier() {
        return tier;
    }
}
