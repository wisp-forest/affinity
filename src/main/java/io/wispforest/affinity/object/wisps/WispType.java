package io.wispforest.affinity.object.wisps;

import io.wispforest.owo.ops.TextOps;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * The type of a Wisp entity. Used to determine the
 * translation key as well as the color of the particles
 * as well as the dropped matter
 */
public interface WispType {

    /**
     * @return The color of the Wisp's particles
     */
    int color();

    String translationKey();

    /**
     * The icon character/emoji of this type,
     * displayed in the tooltip of the matter item
     *
     * @return A single unicode character/emoji
     */
    String icon();

    /**
     * @return How much flux Wisp Matter of
     * this type should produce per second when being harvested
     * in the Matter Harvesting Hearth
     */
    int aethumFluxPerSecond();

    /**
     * @return The Wisp Mist item corresponding
     * to this type
     */
    Item mistItem();

    default Text createTooltip() {
        return TextOps.withColor(this.icon(), this.color()).append(" ").append(Text.translatable(this.translationKey()).formatted(Formatting.GRAY));
    }
}
