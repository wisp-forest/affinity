package io.wispforest.affinity.object.attunedshards;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.List;

public class CustomShardTierJsonFile {

    public static final Endec<CustomShardTierJsonFile> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.ofRegistry(Registries.ITEM).listOf().fieldOf("items",CustomShardTierJsonFile::getItems),
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
