package io.wispforest.affinity.client.render;

import io.wispforest.affinity.Affinity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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

    record Entry(Text text, @Nullable Identifier texture, int x, int y) {

        public static final Identifier DEFAULT_TEXTURE = Affinity.id("textures/gui/tooltip_icons.png");

        public Entry(Text text, int x, int y) {
            this(text, DEFAULT_TEXTURE, x, y);
        }
    }

}
