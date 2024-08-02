package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import io.wispforest.endec.Endec;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class AttunedShardItem extends Item {

    public static final ComponentType<Float> HEALTH = Affinity.component("attuned_shard_health", Endec.FLOAT);

    private final AttunedShardTier tier;

    public AttunedShardItem(AttunedShardTier tier) {
        super(AffinityItems.settings());
        this.tier = tier;
    }

    public AttunedShardTier tier() {
        return tier;
    }

    public static void damageShard(ItemStack shard, float damage) {
        shard.set(HEALTH, MathHelper.clamp(getShardHealth(shard) - damage, 0f, 1f));
    }

    public static float getShardHealth(ItemStack shard) {
        return shard.getOrDefault(HEALTH, 1f);
    }
}
