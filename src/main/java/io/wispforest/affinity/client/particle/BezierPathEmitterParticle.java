package io.wispforest.affinity.client.particle;

import io.wispforest.affinity.particle.BezierPathEmitterParticleEffect;
import io.wispforest.affinity.particle.BezierPathParticleEffect;
import io.wispforest.owo.util.VectorRandomUtils;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BezierPathEmitterParticle extends NoRenderParticle {

    private static final Map<Vec3d, BezierPathEmitterParticle> ACTIVE_PARTICLES = new HashMap<>();

    private final Vec3d position;
    private final ParticleEffect subject;
    private final Vec3d endpoint;
    private final int travelDuration;
    private final boolean randomPath;

    protected BezierPathEmitterParticle(ClientWorld world, double x, double y, double z, ParticleEffect subject, Vec3d endpoint, int emitterDuration, int travelDuration, boolean randomPath) {
        super(world, x, y, z);
        this.endpoint = endpoint;

        this.position = new Vec3d(this.x, this.y, this.z);
        this.maxAge = emitterDuration;
        this.gravityStrength = 0;
        this.subject = subject;
        this.travelDuration = travelDuration;
        this.randomPath = randomPath;

        ACTIVE_PARTICLES.put(this.position, this);
    }

    @Override
    public void tick() {
        var offset = VectorRandomUtils.getRandomOffset(this.world, Vec3d.ZERO, .25);

        this.world.addParticle(new BezierPathParticleEffect(subject, this.endpoint, this.travelDuration, this.randomPath),
                this.x + offset.x, this.y + offset.y, this.z + offset.z, 0, 0, 0);

        super.tick();
    }

    @Override
    public void markDead() {
        super.markDead();
        ACTIVE_PARTICLES.remove(this.position);
    }

    public static void removeParticleAt(Vec3d position) {
        if (!ACTIVE_PARTICLES.containsKey(position)) return;

        var particle = ACTIVE_PARTICLES.get(position);
        particle.age = particle.maxAge;
    }

    public static class Factory implements ParticleFactory<BezierPathEmitterParticleEffect> {
        @Nullable
        @Override
        public Particle createParticle(BezierPathEmitterParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new BezierPathEmitterParticle(world, x, y, z, parameters.effect(), parameters.splineEndpoint(), parameters.emitterDuration(), parameters.travelDuration(), parameters.randomPath());
        }
    }
}
