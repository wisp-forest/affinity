package io.wispforest.affinity.client.particle;

import io.wispforest.affinity.particle.BezierItemParticleEffect;
import io.wispforest.affinity.util.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.CrackParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class BezierItemParticle extends CrackParticle {

    private final Vec3d from;
    private final Vec3d c1;
    private final Vec3d c2;
    private final Vec3d to;

    protected BezierItemParticle(ClientWorld world, double x, double y, double z, ItemStack stack, Vec3d target) {
        super(world, x, y, z, stack);

        this.maxAge = 25;
        this.gravityStrength = 0;

        this.from = new Vec3d(x, y, z);
        this.to = target;

        final var diff = this.to.subtract(this.from);
        c1 = this.from.add(diff.rotateY((float) Math.toRadians(45)).multiply(.4));
        c2 = this.from.add(diff.rotateY((float) Math.toRadians(-45)).multiply(.8));
    }

    @Override
    public void tick() {
        super.tick();

        final double t = this.age / (float) this.maxAge;
        final var pos = MathUtil.bezier_3(t, this.from, this.c1, this.c2, this.to);

        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;

//        MinecraftClient.getInstance().world.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, this.x, this.y, this.z, 0, 0, 0);
    }

    public static class Factory implements ParticleFactory<BezierItemParticleEffect> {
        @Nullable
        @Override
        public Particle createParticle(BezierItemParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new BezierItemParticle(world, x, y, z, parameters.stack(), parameters.gravityCenter());
        }
    }
}
