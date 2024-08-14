package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class AttunedShardItem extends Item {

    public static final KeyedEndec<Float> HEALTH_KEY = Endec.FLOAT.keyed("Health", 1f);

    private final AttunedShardTier tier;

    public AttunedShardItem(Settings settings, AttunedShardTier tier) {
        super(settings);
        this.tier = tier;
    }

    public AttunedShardTier tier() {
        return tier;
    }

    public static void damageShard(ItemStack shard, float damage) {
        shard.put(HEALTH_KEY, MathHelper.clamp(getShardHealth(shard) - damage, 0f, 1f));
    }

    public static float getShardHealth(ItemStack shard) {
        return shard.get(HEALTH_KEY);
    }
}
