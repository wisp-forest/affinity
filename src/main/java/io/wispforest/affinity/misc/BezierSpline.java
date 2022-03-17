package io.wispforest.affinity.misc;

import io.wispforest.affinity.misc.util.MathUtil;
import net.minecraft.util.math.Vec3d;

public class BezierSpline {

    private Vec3d[] values;

    private final Vec3d from;
    private final Vec3d c1;
    private final Vec3d c2;
    private final Vec3d to;

    public static BezierSpline compute(Vec3d from, Vec3d c1, Vec3d c2, Vec3d to, int resolution) {
        var spline = new BezierSpline(from, c1, c2, to);
        spline.compute(resolution);
        return spline;
    }

    private BezierSpline(Vec3d from, Vec3d c1, Vec3d c2, Vec3d to) {
        this.from = from;
        this.c1 = c1;
        this.c2 = c2;
        this.to = to;
    }

    private void compute(int resolution) {
        final double step = 1d / resolution;
        this.values = new Vec3d[resolution];

        for (int i = 0; i < resolution; i++) {
            values[i] = MathUtil.bezier_3(step * i, from, c1, c2, to);
        }
    }

    public Vec3d get(int index) {
        return this.values[index];
    }

    public int resolution() {
        return values.length;
    }

}
