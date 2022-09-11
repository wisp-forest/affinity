package io.wispforest.affinity.client.particle;

import io.wispforest.affinity.particle.GenericEmitterParticleEffect;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class GenericEmitterParticle extends NoRenderParticle {

    private final GenericEmitterParticleEffect parameters;

    public GenericEmitterParticle(ClientWorld clientWorld, double x, double y, double z, GenericEmitterParticleEffect parameters) {
        super(clientWorld, x, y, z);
        this.parameters = parameters;
        this.maxAge = parameters.emitterLifetime();
    }

    @Override
    public void tick() {
        if (this.age % this.parameters.emitInterval() == 0) {
            final var deviation = this.parameters.emitDeviation();

            final var velocityParam = this.parameters.emitVelocity();
            final var velocity = this.parameters.randomizeVelocity()
                    ? new Vec3d(
                    this.random.nextFloat() * velocityParam.x,
                    this.random.nextFloat() * velocityParam.y,
                    this.random.nextFloat() * velocityParam.z)
                    : velocityParam;

            this.world.addParticle(
                    this.parameters.effect(),
                    this.x + this.random.nextFloat() * deviation - deviation * .5,
                    this.y + this.random.nextFloat() * deviation - deviation * .5,
                    this.z + this.random.nextFloat() * deviation - deviation * .5,
                    velocity.x,
                    velocity.y,
                    velocity.z
            );
        }

        if (++this.age > this.maxAge) {
            this.markDead();
        }
    }

    public static class Factory implements ParticleFactory<GenericEmitterParticleEffect> {

        @Nullable
        @Override
        public Particle createParticle(GenericEmitterParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new GenericEmitterParticle(world, x, y, z, parameters);
        }
    }
}
