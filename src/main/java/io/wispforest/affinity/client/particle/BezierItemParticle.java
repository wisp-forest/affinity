package io.wispforest.affinity.client.particle;

import io.wispforest.affinity.particle.BezierItemParticleEffect;
import io.wispforest.affinity.util.BezierSpline;
import net.minecraft.client.particle.CrackParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class BezierItemParticle extends CrackParticle {

    private final BezierSpline spline;

    public BezierItemParticle(ClientWorld world, double x, double y, double z, ItemStack stack, Vec3d endpoint) {
        super(world, x, y, z, stack);

        this.maxAge = 15;
        this.gravityStrength = 0;

        this.spline = makePath(new Vec3d(x, y, z), endpoint, this.maxAge);
    }

    @Override
    public void tick() {
        super.tick();

        final var pos = this.spline.get(Math.min(this.age, this.spline.resolution() - 1));

        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public static BezierSpline makePath(Vec3d from, Vec3d to, int resolution) {
        final var diff = to.subtract(from);
        final var c1 = from.add(diff.add(0, 3, 0).rotateY((float) Math.toRadians(-45)).multiply(.5));
        final var c2 = from.add(diff.add(0, 2, 0).rotateY((float) Math.toRadians(45)).multiply(.5));

        return BezierSpline.compute(from, c1, c2, to, resolution);
    }

    public static class Factory implements ParticleFactory<BezierItemParticleEffect> {
        @Nullable
        @Override
        public Particle createParticle(BezierItemParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new BezierItemParticle(world, x, y, z, parameters.stack(), parameters.splineEndpoint());
        }
    }
}
