package io.wispforest.affinity.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;

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

    public static double distance(BlockPos pos, BlockPos other) {
        return Math.sqrt(pos.getSquaredDistance(other, false));
    }

    public static String rounded(double value, int places) {
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).toString();
    }

    public static Vec3f splitRGBToVector(int rgb) {
        return new Vec3f((rgb >> 16) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
    }

    public static float[] splitRGBToFloats(int rgb) {
        return new float[]{(rgb >> 16) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f};
    }

    public static int[] splitRGBToInts(int rgb) {
        return new int[]{rgb >> 16, (rgb >> 8) & 0xFF, rgb & 0xFF};
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

