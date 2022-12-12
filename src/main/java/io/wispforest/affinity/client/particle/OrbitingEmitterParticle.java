package io.wispforest.affinity.client.particle;

import io.wispforest.affinity.particle.OrbitingEmitterParticleEffect;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OrbitingEmitterParticle extends NoRenderParticle {

    private final OrbitingEmitterParticleEffect parameters;

    protected OrbitingEmitterParticle(ClientWorld clientWorld, double x, double y, double z, OrbitingEmitterParticleEffect parameters) {
        super(clientWorld, x, y, z);
        this.parameters = parameters;

        this.velocityX = parameters.speed().x();
        this.velocityY = parameters.speed().y();
        this.velocityZ = parameters.speed().z();

        this.gravityStrength = 0;
        this.velocityMultiplier = 1;

        this.maxAge = parameters.lifetime();
    }

    @Override
    public void tick() {
        if (this.age % this.parameters.emitInterval() == 0) {

            var other = new Vector3f(this.parameters.speed());
            other.add(34, 35, 69);

            var offset = new Vector3f(this.parameters.speed());
            offset.cross(other);
            offset.normalize();
            offset.mul(this.parameters.radius());

            final var axis = this.parameters.speed();
            axis.normalize();

            offset.rotate(new Quaternionf().rotateAxis(this.age * this.parameters.orbitSpeed() % 360, axis));

            this.world.addParticle(
                    this.parameters.innerEffect(),
                    this.x, this.y, this.z,
                    0, 0, 0
            );

            this.world.addParticle(
                    this.parameters.outerEffect(),
                    this.x + offset.x(), this.y + offset.y(), this.z + offset.z(),
                    0, 0, 0
            );
        }

        super.tick();
    }

    public static class Factory implements ParticleFactory<OrbitingEmitterParticleEffect> {

        @Nullable
        @Override
        public Particle createParticle(OrbitingEmitterParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new OrbitingEmitterParticle(world, x, y, z, parameters);
        }
    }
}
