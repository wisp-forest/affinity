package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.AberrantCallingCoreBlock;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.particle.BezierPathEmitterParticleEffect;
import io.wispforest.affinity.particle.GenericEmitterParticleEffect;
import io.wispforest.affinity.particle.OrbitingEmitterParticleEffect;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.List;

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
        ClientParticles.spawnLine(new DustParticleEffect(MathUtil.splitRGBToVec3f(data.color()), 1), world, pos, data.target(), .15f);
    });

    public static final ParticleSystem<BlockPos> TIME_STAFF_ACCELERATE = CONTROLLER.register(BlockPos.class, (world, pos, data) -> {
        ClientParticles.spawn(
                new OrbitingEmitterParticleEffect(
                        new DustParticleEffect(MathUtil.splitRGBToVec3f(0x3955E5), .75f),
                        ParticleTypes.ASH,
                        Vec3d.ofCenter(data).subtract(pos).multiply(1f / 20f).toVector3f(),
                        .1f, 1, 45, 20
                ),
                world, pos, 0
        );

        ClientParticles.setParticleCount(5);
        ClientParticles.spawnCubeOutline(new DustParticleEffect(MathUtil.splitRGBToVec3f(0x7743DB), .6f), world, Vec3d.of(data), 1, .05f);
    });

    public static final ParticleSystem<Vec3d> ABERRANT_CORE_HINT = CONTROLLER.register(Vec3d.class, (world, pos, data) -> {
        var length = data.subtract(pos).length();

        ClientParticles.setParticleCount((int) Math.round(length * 5));
        ClientParticles.spawnLine(ParticleTypes.SMOKE, world, pos, data, .05f);
    });

    public static final ParticleSystem<DissolveData> DISSOLVE_ITEM = CONTROLLER.register(DissolveData.class, (world, pos, data) -> {
        world.addParticle(BezierPathEmitterParticleEffect.item(data.suckWhat(), data.suckWhere(), data.particleMaxAge(), data.duration()),
                pos.x, pos.y, pos.z, 0, 0, 0);

        world.addParticle(new GenericEmitterParticleEffect(
                new ItemStackParticleEffect(ParticleTypes.ITEM, data.suckWhat()),
                new Vec3d(.05f, 0.2f, .05f), 1, .15f, true, data.duration()
        ), pos.x, pos.y, pos.z, 0, 0, 0);
    });

    public static final ParticleSystem<Void> DRIPPING_AZALEA = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(5);
        ClientParticles.spawnPrecise(ParticleTypes.FALLING_SPORE_BLOSSOM, world, pos, 1, 1.5, 1);
        ClientParticles.setParticleCount(5);
        ClientParticles.spawnPrecise(ParticleTypes.FALLING_NECTAR, world, pos, 1, 1.5, 1);
    });

    public static final ParticleSystem<Void> BANISHMENT_CLOUD = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(20);
        ClientParticles.spawn(ParticleTypes.EFFECT, world, pos, 0.5);
    });

    public static final ParticleSystem<AberrantCallingCoreBlock.CoreSet> ABERRANT_CALLING_ACTIVE = CONTROLLER.register(AberrantCallingCoreBlock.CoreSet.class, (world, pos, data) -> {
        var effect = new DustColorTransitionParticleEffect(new Vector3f(1, 0, 0), new Vector3f(1, .25f, .75f), 1);

        ClientParticles.persist();
        ClientParticles.setParticleCount(7);

        ClientParticles.spawnLine(effect, world, pos, Vec3d.ofCenter(data.get(0)), .05f);
        ClientParticles.spawnLine(effect, world, pos, Vec3d.ofCenter(data.get(2)), .05f);
        ClientParticles.spawnLine(effect, world, Vec3d.ofCenter(data.get(1)), Vec3d.ofCenter(data.get(0)), .05f);
        ClientParticles.spawnLine(effect, world, Vec3d.ofCenter(data.get(1)), Vec3d.ofCenter(data.get(2)), .05f);

        ClientParticles.reset();
    });

    public static final ParticleSystem<Void> ABERRANT_CALLING_SUCCESS = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.spawn(new GenericEmitterParticleEffect(
                ParticleTypes.LARGE_SMOKE, new Vec3d(0, .25, 0), 1, .5f, false, 8
        ), world, pos, 0d);

        ClientParticles.setParticleCount(10);
        ClientParticles.spawn(ParticleTypes.LAVA, world, pos, .25);
    });


    public static final ParticleSystem<CandleData> AFFINE_CANDLE_BREWING = CONTROLLER.register(CandleData.class, (world, pos, data) -> {
        for (var candle : data.candles()) {
            ClientParticles.spawn(new BezierPathEmitterParticleEffect(ParticleTypes.REVERSE_PORTAL, pos, 30, 20), world, candle, .15f);
        }
    });

    public static final ParticleSystem<Void> LIGHT_BLOCK = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(6);
        ClientParticles.spawnCenteredOnBlock(ParticleTypes.FLAME, world, new BlockPos(pos), 1.25);

        ClientParticles.setParticleCount(15);
        ClientParticles.spawnCenteredOnBlock(ParticleTypes.SMOKE, world, new BlockPos(pos), 1.25);
    });

    public record DissolveData(ItemStack suckWhat, Vec3d suckWhere, int duration, int particleMaxAge) {}

    public record LineData(Vec3d target, int color) {}

    public record CandleData(List<Vec3d> candles) {}

    public static void initialize() {}
}
