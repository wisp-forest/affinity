package io.wispforest.affinity.client.render;

import io.wispforest.affinity.Affinity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * An interface to be implemented on {@link net.minecraft.block.entity.BlockEntity} types that
 * should display some stats in the world when targeted by the player
 */
public interface InWorldTooltipProvider {

    /**
     * Update the tooltip entries display by this provider,
     * usually used for visually interpolated values
     *
     * @param force Whether all interpolated values should instantaneously
     *              be updated to their real current value, usually because the
     *              player's targeted block has changed
     * @param delta The duration of the last frame, in partial ticks
     */
    default void updateTooltipEntries(boolean force, float delta) {}

    /**
     * Optionally apply an offset to the anchor
     * position for this provider's tooltip. A copy
     * of {@code tooltipPos} with the desired offset
     * shall be returned
     */
    default Vec3d applyTooltipOffset(Vec3d tooltipPos) {
        return tooltipPos;
    }

    /**
     * The statistics this provider should currently
     * display, wrapped in {@link Entry}
     */
    void appendTooltipEntries(List<Entry> entries);

    sealed interface Entry permits TextEntry, TextAndIconEntry {
        Text label();

        static Entry icon(Text text, int u, int v) {
            return new TextAndIconEntry(text, TextAndIconEntry.DEFAULT_TEXTURE, u, v);
        }

        static Entry icon(Text text, Identifier texture, int u, int v) {
            return new TextAndIconEntry(text, texture, u, v);
        }

        static Entry text(Text icon, Text text) {
            return new TextEntry(icon, text);
        }
    }

    record TextEntry(Text icon, Text label) implements Entry {}

    record TextAndIconEntry(Text label, Identifier texture, int u, int v) implements Entry {
        public static final Identifier DEFAULT_TEXTURE = Affinity.id("textures/gui/tooltip_icons.png");
    }

}
