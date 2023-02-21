package io.wispforest.affinity.misc;

import io.wispforest.owo.ui.util.Delta;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.concurrent.ThreadLocalRandom;

public class AstrokinesisStar {

    private final float polar, azimuthal, size;
    private float displayPolar, displayAzimuthal;

    private float alpha = 1f;
    private boolean canBeFrozen = false;

    public boolean frozen = false;

    public AstrokinesisStar() {
        this.polar = ThreadLocalRandom.current().nextFloat() * 90;
        this.azimuthal = ThreadLocalRandom.current().nextFloat() * 360;
        this.size = ThreadLocalRandom.current().nextFloat(.25f, .6f);
    }

    public void update(@Nullable Vector2f target, float delta) {
        if (this.frozen) return;

        if (target != null) {
            float polarDifference = this.distance(this.displayPolar, target.x);
            float azimuthalDifference = this.distance(this.displayAzimuthal, target.y);

            float absPolarDifference = Math.abs(polarDifference);
            float absAzimuthalDifference = Math.abs(azimuthalDifference);

            if (absPolarDifference <= 30 && absAzimuthalDifference <= 30) {
                this.displayPolar += polarDifference * delta * .5f;
                this.displayAzimuthal += azimuthalDifference * delta * .5f;

                this.alpha = this.smoothstep(.8f, 2, 5 / Math.min(absPolarDifference, absAzimuthalDifference));

                this.canBeFrozen = absPolarDifference <= 2 && absAzimuthalDifference <= 2;
                return;
            }
        }

        this.displayPolar += this.computeDelta(this.displayPolar, this.polar, delta);
        this.displayAzimuthal += this.computeDelta(this.displayAzimuthal, this.azimuthal, delta);
        this.alpha += Delta.compute(this.alpha, 0, delta);
        this.canBeFrozen = false;
    }

    private float smoothstep(float edge0, float edge1, float x) {
        if (x < edge0) return 0;
        if (x >= edge1) return 1;

        x = (x - edge0) / (edge1 - edge0);
        return x * x * (3 - 2 * x);
    }

    private float computeDelta(float current, float target, float delta) {
        return distance(current, target) * delta * .5f;
    }

    private float distance(float a, float b) {
        float delta1 = b - a;
        float delta2 = (b - 360) - a;
        float delta3 = (b + 360) - a;

        if (Math.abs(delta1) < Math.abs(delta2) && Math.abs(delta1) < Math.abs(delta3)) {
            return delta1;
        } else if (Math.abs(delta2) < Math.abs(delta3)) {
            return delta2;
        } else {
            return delta3;
        }
    }

    public boolean canBeFrozen() {
        return this.canBeFrozen;
    }

    public float alpha() {
        return this.alpha;
    }

    public float polar() {
        return this.displayPolar;
    }

    public float azimuthal() {
        return this.displayAzimuthal;
    }

    public float size() {
        return size;
    }
}
