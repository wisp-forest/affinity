package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.minecraft.item.Item;

public class GeologicalResonatorItem extends Item {

    public GeologicalResonatorItem() {
        super(new OwoItemSettings().tab(0).group(Affinity.AFFINITY_GROUP).maxCount(1));
    }

}
