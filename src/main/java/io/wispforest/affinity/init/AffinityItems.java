package io.wispforest.affinity.init;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.shards.AttunedShardTiers;
import io.wispforest.affinity.item.*;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.SignItem;

@SuppressWarnings("unused")
public class AffinityItems implements ItemRegistryContainer {

    public static final Item AETHUM_FLUX_BOTTLE = new AethumFluxBottleItem();

    public static final Item MILDLY_ATTUNED_AMETHYST_SHARD = new AttunedShardItem(AttunedShardTiers.MILDLY_ATTUNED);
    public static final Item FAIRLY_ATTUNED_AMETHYST_SHARD = new AttunedShardItem(AttunedShardTiers.FAIRLY_ATTUNED);
    public static final Item GREATLY_ATTUNED_AMETHYST_SHARD = new AttunedShardItem(AttunedShardTiers.GREATLY_ATTUNED);

    public static final Item EMERALD_WAND_OF_IRIDESCENCE = new IridescenceWandItem();
    public static final Item SAPPHIRE_WAND_OF_IRIDESCENCE = new IridescenceWandItem();
    public static final Item GEOLOGICAL_RESONATOR = new GeologicalResonatorItem();

    public static final Item AZALEA_FLOWERS = new Item(new OwoItemSettings().tab(1).group(Affinity.AFFINITY_GROUP));

    public static final Item INERT_WISP_MATTER = new WispMatterItem(AffinityWispTypes.INERT);
    public static final Item WISE_WISP_MATTER = new WispMatterItem(AffinityWispTypes.WISE);
    public static final Item VICIOUS_WISP_MATTER = new WispMatterItem(AffinityWispTypes.VICIOUS);

    public static final Item AZALEA_SIGN = new SignItem(new OwoItemSettings().group(Affinity.AFFINITY_GROUP)
            .maxCount(16).tab(1), AffinityBlocks.AZALEA_SIGN, AffinityBlocks.AZALEA_WALL_SIGN);
    public static final Item AZALEA_BOAT = new BoatItem(AffinityBlocks.AZALEA_BOAT_TYPE,
            new OwoItemSettings().tab(1).group(Affinity.AFFINITY_GROUP).maxCount(1));
}
