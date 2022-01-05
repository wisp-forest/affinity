package io.wispforest.affinity.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3f;

public class MathUtil {

    @Environment(EnvType.CLIENT)
    public static float proportionalApproach(float value, float targetValue, float minChange, float coefficient) {
        float diff = value - targetValue;

        if (Math.abs(diff) > minChange) {
            return value - diff * coefficient * (MinecraftClient.getInstance().getLastFrameDuration() / .005f);
        } else {
            return targetValue;
        }
    }

    public static Vec3f unpackRGB(int rgb) {
        return new Vec3f((rgb >> 16) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
    }

}
