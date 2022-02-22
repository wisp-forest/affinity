package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import net.minecraft.item.Item;

public class SocleOrnamentItem extends Item {

    private final RitualSocleType socleType;

    public SocleOrnamentItem(RitualSocleType socleType) {
        super(AffinityItems.settings(0));
        this.socleType = socleType;
    }

    public RitualSocleType socleType() {
        return socleType;
    }
}
