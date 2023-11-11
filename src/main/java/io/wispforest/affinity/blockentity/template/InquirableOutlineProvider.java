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

        public Outline(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX + 1;
            this.maxY = maxY + 1;
            this.maxZ = maxZ + 1;
        }

        public static Outline symmetrical(int x, int y, int z) {
            return new Outline(-x, -y, -z, x, y, z);
        }
    }
}
