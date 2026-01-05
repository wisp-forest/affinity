package io.wispforest.affinity.object.attunedshards;

import io.wispforest.affinity.endec.BuiltInEndecs;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.List;

public class CustomShardTierJsonFile {

    public static final Endec<CustomShardTierJsonFile> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.ofRegistry(Registries.ITEM).listOf().fieldOf("items",CustomShardTierJsonFile::getItems),
            CustomShardTier.ENDEC.fieldOf("tier", CustomShardTierJsonFile::getTier),
            CustomShardTierJsonFile::new
    );

    private List<Item> items;
    private CustomShardTier tier;

    public CustomShardTierJsonFile (List<Item> items, CustomShardTier tier) {
        this.items = items;
        this.tier = tier;
    }

    public List<Item> getItems() {
        return items;
    }

    public CustomShardTier getTier() {
        return tier;
    }
}
