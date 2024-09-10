package io.wispforest.affinity.client.misc;

import io.wispforest.owo.ui.util.Delta;

public class Interpolator {

    private final float defaultValue;

    private float value;
    private float target;

    public Interpolator(double value) {
        this.defaultValue = this.target = this.value = (float) value;
    }

    public static void update(float delta, Interpolator... interpolators) {
        for (var interpolator : interpolators) {
            interpolator.value += Delta.compute(interpolator.value, interpolator.target, delta);
        }
    }

    public static void reset(Interpolator... interpolators) {
        for (var interpolator : interpolators) {
            interpolator.target = interpolator.defaultValue;
        }
    }

    public void targetAdd(double value) {
        this.target += value;
    }

    public void set(double value) {
        this.target = this.value = (float) value;
    }

    public float get() {
        return this.value;
    }
}
