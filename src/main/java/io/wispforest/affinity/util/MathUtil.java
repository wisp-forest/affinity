package io.wispforest.affinity.util;

public class MathUtil {

    public static float sinLerp(float start, float end, float x) {
        float delta = (float) (Math.sin(x * Math.PI - Math.PI / 2) * .5 + .5);
        return start + delta * (end - start);
    }

}
