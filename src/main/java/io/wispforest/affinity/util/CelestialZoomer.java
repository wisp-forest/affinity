package io.wispforest.affinity.util;

import net.minecraft.world.World;

public class CelestialZoomer {

    public static long serverTimeOfDay = 0;
    public static long lastWorldTime = 0;
    private static long offsetTime = 0;

    private static boolean isZooming = false;
    private static boolean offsetEnabled = false;

    public static void enableOffset(long time) {
        offsetTime = time;
        isZooming = true;
        offsetEnabled = true;
    }

    public static void disableOffset() {
        isZooming = true;
        offsetEnabled = false;
    }

    public static boolean isZooming() {
        return isZooming;
    }

    public static boolean offsetEnabled() {
        return offsetEnabled || isZooming;
    }

    public static long getZoomedTime(World world) {
        long actual = world.getTimeOfDay();

        long target = offsetEnabled ? offsetTime : serverTimeOfDay;

        if (target < 0) actual = -actual;

        final var zoomedTime = Math.round(interpolate(actual, target, 20, .05));
        isZooming = zoomedTime != target;
        return zoomedTime;
    }

    @SuppressWarnings("SameParameterValue")
    private static double interpolate(double value, double targetValue, double minChange, double coefficient) {
        double diff = value - targetValue;

        if (Math.abs(diff) > minChange) {
            return value - diff * coefficient;
        } else {
            return targetValue;
        }
    }
}
