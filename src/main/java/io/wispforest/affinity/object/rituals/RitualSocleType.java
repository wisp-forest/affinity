package io.wispforest.affinity.object.rituals;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface RitualSocleType {

    /**
     * @return The color the glow overlay on the
     * socle's model should have
     */
    int glowColor();

    /**
     * @return The amount by which a socle of this type
     * boosts the stability of a ritual, in % of (100 - stability)
     */
    double stabilityModifier();

    /**
     * @return The item used to craft socles of this type,
     * also used when deconstructing a socle of this type
     */
    Item ornamentItem();

    /**
     * @return The actual socle block that this type represents
     */
    Block socleBlock();

}
