package io.wispforest.affinity.client.render;

import io.wispforest.affinity.Affinity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * An interface to be implemented on {@link net.minecraft.block.entity.BlockEntity} types that
 * should display some stats under the crosshair when looked at
 */
public interface CrosshairStatProvider {

    /**
     * @return {@code true} if this provider is currently
     * providing meaningful stats to display
     */
    default boolean shouldDisplay() {
        return true;
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
