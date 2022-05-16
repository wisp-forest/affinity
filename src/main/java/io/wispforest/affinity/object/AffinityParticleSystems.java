package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.particle.BezierItemEmitterParticleEffect;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import net.minecraft.item.ItemStack;
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

    public static final ParticleSystem<Vec3d> ABERRANT_CORE_HINT = CONTROLLER.register(Vec3d.class, (world, pos, data) -> {
        var length = data.subtract(pos).length();

        ClientParticles.setParticleCount((int) Math.round(length * 5));
        ClientParticles.spawnLine(ParticleTypes.SMOKE, world, pos, data, .05f);
    });

    public static final ParticleSystem<DissolveData> DISSOLVE_ITEM = CONTROLLER.register(DissolveData.class, (world, pos, data) -> {
        world.addParticle(new BezierItemEmitterParticleEffect(data.suckWhat(), data.suckWhere(), data.particleMaxAge(), data.duration()),
                pos.x, pos.y, pos.z, 0, 0, 0);
    });

    public static final ParticleSystem<Void> DRIPPING_AZALEA = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(5);
        ClientParticles.spawnPrecise(ParticleTypes.FALLING_SPORE_BLOSSOM, world, pos, 1, 1.5, 1);
        ClientParticles.setParticleCount(5);
        ClientParticles.spawnPrecise(ParticleTypes.FALLING_NECTAR, world, pos, 1, 1.5, 1);
    });

    public static final ParticleSystem<Void> TRANSPORTATION_CLOUD = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(20);
        ClientParticles.spawn(ParticleTypes.EFFECT, world, pos, 0.5);
    });

    public record DissolveData(ItemStack suckWhat, Vec3d suckWhere, int duration, int particleMaxAge) {}

    public record LineData(Vec3d target, int color) {}

    public static void initialize() {}
}
