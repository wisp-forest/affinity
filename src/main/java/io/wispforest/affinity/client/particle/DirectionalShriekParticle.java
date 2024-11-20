package io.wispforest.affinity.client.particle;

import io.wispforest.affinity.particle.DirectionalShriekParticleEffect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ShriekParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

public class DirectionalShriekParticle extends ShriekParticle {

    private final Direction direction;

    protected DirectionalShriekParticle(ClientWorld world, double x, double y, double z, int delay, Direction direction) {
        super(world, x, y, z, delay);
        this.direction = direction;

        this.velocityX = direction.getOffsetX() * .1;
        this.velocityY = direction.getOffsetY() * .1;
        this.velocityZ = direction.getOffsetZ() * .1;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        if (this.delay <= 0) {
            var facing = new Quaternionf(this.direction.getOpposite().getRotationQuaternion());

            this.alpha = 1.0F - MathHelper.clamp(((float) this.age + tickDelta) / (float) this.maxAge, 0.0F, 1.0F);
            Quaternionf quaternionf = new Quaternionf(facing);
            quaternionf.rotateX(-1.0472F);
            this.method_60373(vertexConsumer, camera, quaternionf, tickDelta);
            quaternionf.set(facing);
            quaternionf.rotateYXZ((float) -Math.PI, 1.0472F, 0.0F);
            this.method_60373(vertexConsumer, camera, quaternionf, tickDelta);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DirectionalShriekParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DirectionalShriekParticleEffect effect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            ShriekParticle shriekParticle = new DirectionalShriekParticle(clientWorld, d, e, f, effect.delay(), effect.direction());
            shriekParticle.setSprite(this.spriteProvider);
            return shriekParticle;
        }
    }
}
