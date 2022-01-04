package io.wispforest.affinity.util;

import net.minecraft.world.World;

public class CelestialZoomer {

    public static long serverTimeOfDay = 0;
    private static long lastWorldTime = 0;
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

    public static long lastWorldTime() {
        return lastWorldTime;
    }

    public static boolean offsetEnabled() {
        return offsetEnabled || isZooming;
    }

    public static long getZoomedTime(World world) {
        long actual = world.getTimeOfDay();
        lastWorldTime = actual;

        long target = offsetEnabled ? offsetTime : serverTimeOfDay;

        if (target < 0) actual = -actual;

        final var zoomedTime = Math.round(MathUtil.proportionalApproach(actual, target, 10, .05));
        isZooming = zoomedTime != target;
        return zoomedTime;
    }
}
