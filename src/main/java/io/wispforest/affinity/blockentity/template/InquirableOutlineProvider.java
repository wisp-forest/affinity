package io.wispforest.affinity.blockentity.template;

import io.wispforest.affinity.client.render.CuboidRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for block entities that need to display
 * an area of effect or something similar to the user
 * by rendering an axis-aligned cuboid when clicked
 * with the Wand of Inquiry
 */
public interface InquirableOutlineProvider {

    @Environment(EnvType.CLIENT)
    @Nullable CuboidRenderer.Cuboid getActiveOutline();

    record Outline(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public static Outline symmetrical(int x, int y, int z) {
            return new Outline(-x, -y, -z, x + 1, y + 1, z + 1);
        }
    }
}
