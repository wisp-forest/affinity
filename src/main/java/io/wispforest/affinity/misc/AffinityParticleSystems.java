package io.wispforest.affinity.misc;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.util.MathUtil;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

public class AffinityParticleSystems {

    private static final ParticleSystemController CONTROLLER = new ParticleSystemController(Affinity.id("particles"));

    public static final ParticleSystem<Void> FLIGHT_REMOVED = CONTROLLER.register(Void.class, (world, pos, data) -> {
        pos = pos.add(0, 1, 0);

        ClientParticles.setParticleCount(30);
        ClientParticles.randomizeVelocity(2);
        ClientParticles.spawnPrecise(ParticleTypes.ENCHANT, world, pos, .5, 2, .5);

        ClientParticles.setParticleCount(50);
        ClientParticles.spawnPrecise(ParticleTypes.SCRAPE, world, pos, 1, 2, 1);
    });

    public static final ParticleSystem<LineData> WISP_ATTACK = CONTROLLER.register(LineData.class, (world, pos, data) -> {
        var length = data.target().subtract(pos).length();

        ClientParticles.setParticleCount((int) Math.round(length * 5));
        ClientParticles.spawnLine(new DustParticleEffect(MathUtil.splitRGBToVector(data.color()), 1), world, pos, data.target(), .15f);
    });

    public record LineData(Vec3d target, int color) {}

    public static void initialize() {}
}
