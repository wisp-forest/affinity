package io.wispforest.affinity.misc.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public class MathUtil {

    public static double mean(double... values) {
        double mean = 0;
        for (var value : values) mean += value;
        return mean / values.length;
    }

    public static double mean(Collection<Double> values) {
        double mean = 0;
        for (var value : values) mean += value;
        return mean / values.size();
    }

    public static double standardDeviation(double... values) {
        return standardDeviation(mean(values), values);
    }

    public static double standardDeviation(double mean, double... values) {
        double acc = 0;
        for (var value : values) acc += (value - mean) * (value - mean);

        return Math.sqrt((1d / values.length) * acc);
    }

    public static double standardDeviation(Collection<Double> values) {
        return standardDeviation(mean(values), values);
    }

    public static double standardDeviation(double mean, Collection<Double> values) {
        double acc = 0;
        for (var value : values) acc += (value - mean) * (value - mean);

        return Math.sqrt((1d / values.size()) * acc);
    }

    public static double bezier_3(double t, double w0, double w1, double w2, double w3) {
        final double t_2 = t * t;
        final double t_3 = t_2 * t;
        final double m_t = 1 - t;
        final double m_t_2 = m_t * m_t;
        final double m_t_3 = m_t_2 * m_t;
        return w0 * m_t_3 + 3 * w1 * m_t_2 * t + 3 * w2 * m_t * t_2 + w3 * t_3;
    }

    public static Vec3d bezier_3(double t, Vec3d from, Vec3d c1, Vec3d c2, Vec3d to) {
        final double x = bezier_3(t, from.x, c1.x, c2.x, to.x);
        final double y = bezier_3(t, from.y, c1.y, c2.y, to.y);
        final double z = bezier_3(t, from.z, c1.z, c2.z, to.z);
        return new Vec3d(x, y, z);
    }

    public static Vec3d entityCenterPos(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ());
    }

    public static double distance(BlockPos pos, BlockPos other) {
        return Math.sqrt(pos.getSquaredDistance(other));
    }

    public static String rounded(double value, int places) {
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static Vector3f splitRGBToVec3f(int rgb) {
        return new Vector3f((rgb >> 16) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
    }

    public static Vec3d splitRGBToVec3d(int rgb) {
        return new Vec3d((rgb >> 16) / 255d, ((rgb >> 8) & 0xFF) / 255d, (rgb & 0xFF) / 255d);
    }

    public static float[] splitRGBToFloats(int rgb) {
        return new float[]{(rgb >> 16) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f};
    }

    public static int[] splitRGBToInts(int rgb) {
        return new int[]{rgb >> 16, (rgb >> 8) & 0xFF, rgb & 0xFF};
    }

    public static float smoothstep(float edge0, float edge1, float x) {
        if (x < edge0) return 0;
        if (x >= edge1) return 1;

        x = (x - edge0) / (edge1 - edge0);
        return x * x * (3 - 2 * x);
    }

    @Environment(EnvType.CLIENT)
    public static float proportionalApproach(float value, float targetValue, float minChange, float coefficient) {
        float diff = value - targetValue;

        if (Math.abs(diff) > minChange) {
            if (diff > 0) {
                return Math.max(targetValue, value - diff * coefficient * (MinecraftClient.getInstance().getLastFrameDuration() / .005f));
            } else {
                return Math.min(targetValue, value - diff * coefficient * (MinecraftClient.getInstance().getLastFrameDuration() / .005f));
            }
        } else {
            return targetValue;
        }
    }
}

