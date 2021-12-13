package io.wispforest.affinity.registries;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.AethumFluxBottleItem;
import io.wispforest.affinity.item.IridescenceWandItem;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.minecraft.item.Item;

public class AffinityItems implements ItemRegistryContainer {

    public static final Item EMERALD_WAND_OF_IRIDESCENCE = new IridescenceWandItem();
    public static final Item AETHUM_FLUX_BOTTLE = new AethumFluxBottleItem();

    public static final Item MILDLY_ATTUNED_AMETHYST_SHARD = new Item(new Item.Settings().group(Affinity.AFFINITY_GROUP));
    public static final Item FAIRLY_ATTUNED_AMETHYST_SHARD = new Item(new Item.Settings().group(Affinity.AFFINITY_GROUP));
    public static final Item GREATLY_ATTUNED_AMETHYST_SHARD = new Item(new Item.Settings().group(Affinity.AFFINITY_GROUP));

}
