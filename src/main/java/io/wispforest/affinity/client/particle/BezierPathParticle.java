package io.wispforest.affinity.client.particle;

import io.wispforest.affinity.misc.BezierSpline;
import io.wispforest.affinity.mixin.access.ParticleManagerInvoker;
import io.wispforest.affinity.particle.BezierPathParticleEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class BezierPathParticle extends Particle {

    private final BezierSpline spline;
    private final Particle subject;

    public BezierPathParticle(ClientWorld world, double x, double y, double z, Particle subject, Vec3d endpoint, int travelDuration, boolean randomPath) {
        super(world, x, y, z);

        this.maxAge = travelDuration;
        this.gravityStrength = 0;
        this.subject = subject;

        this.spline = makePath(new Vec3d(x, y, z), endpoint, this.maxAge, randomPath);
    }

    @Override
    public void tick() {
        this.subject.tick();
        super.tick();

        this.dead = !this.subject.isAlive();

        final var pos = this.spline.get(Math.min(this.age, this.spline.resolution() - 1));
        this.subject.setPos(pos.x, pos.y, pos.z);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        this.subject.buildGeometry(vertexConsumer, camera, tickDelta);
    }

    @Override
    public ParticleTextureSheet getType() {
        return this.subject.getType();
    }

    protected BezierSpline makePath(Vec3d from, Vec3d to, int resolution, boolean randomize) {
        final var diff = to.subtract(from);

        final Vec3d c1;
        final Vec3d c2;

        if (randomize) {
            c1 = from.add(diff.add(0, -2 + this.random.nextFloat() * 4, 0).rotateY((float) Math.toRadians(-45 + this.random.nextFloat() * 90)).multiply(.5));
            c2 = from.add(diff.add(0, -2 + this.random.nextFloat() * 4, 0).rotateY((float) Math.toRadians(-45 + this.random.nextFloat() * 90)).multiply(.5));
        } else {
            c1 = from.add(diff.add(0, 3, 0).rotateY((float) Math.toRadians(-45)).multiply(.5));
            c2 = from.add(diff.add(0, 2, 0).rotateY((float) Math.toRadians(45)).multiply(.5));
        }

        return BezierSpline.compute(from, c1, c2, to, resolution);
    }

    public static class Factory implements ParticleFactory<BezierPathParticleEffect> {
        @Nullable
        @Override
        public Particle createParticle(BezierPathParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            final var particle = ((ParticleManagerInvoker) MinecraftClient.getInstance().particleManager)
                    .affinity$createParticle(parameters.effect(), x, y, z, velocityX, velocityY, velocityZ);
            particle.setMaxAge(parameters.travelDuration() + 1);

            return new BezierPathParticle(world, x, y, z, particle, parameters.splineEndpoint(), parameters.travelDuration(), parameters.randomPath());
        }
    }
}
