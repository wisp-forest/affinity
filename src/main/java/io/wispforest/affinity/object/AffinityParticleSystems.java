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
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
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
        var length = data.target.subtract(pos).length();

        ClientParticles.setParticleCount((int) Math.round(length * 5));
        ClientParticles.spawnLine(new DustParticleEffect(MathUtil.splitRGBToVec3f(data.color), 1), world, pos, data.target, .15f);
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
        world.addParticle(BezierPathEmitterParticleEffect.item(data.suckWhat, data.suckWhere, data.particleMaxAge, data.duration, false),
                pos.x, pos.y, pos.z, 0, 0, 0);

        world.addParticle(new GenericEmitterParticleEffect(
                new ItemStackParticleEffect(ParticleTypes.ITEM, data.suckWhat),
                new Vec3d(.05, 0.2, .05), 1, .15f, true, data.duration
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

    public static final ParticleSystem<Void> LAVA_ERUPTION = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.spawn(new GenericEmitterParticleEffect(
                ParticleTypes.LARGE_SMOKE, new Vec3d(0, .25, 0), 1, .5f, false, 8
        ), world, pos, 0d);

        ClientParticles.setParticleCount(10);
        ClientParticles.spawn(ParticleTypes.LAVA, world, pos, .25);
    });


    public static final ParticleSystem<BezierVortexData> BEZIER_VORTEX = CONTROLLER.register(BezierVortexData.class, (world, pos, data) -> {
        for (var candle : data.originPositions) {
            ClientParticles.spawn(new BezierPathEmitterParticleEffect(data.particle, pos, data.travelDuration, data.emitDuration, data.randomPath), world, candle, .15f);
        }
    });

    public static final ParticleSystem<Void> LIGHT_BLOCK = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(6);
        ClientParticles.spawnCenteredOnBlock(ParticleTypes.FLAME, world, new BlockPos(pos), 1.25);

        ClientParticles.setParticleCount(15);
        ClientParticles.spawnCenteredOnBlock(ParticleTypes.SMOKE, world, new BlockPos(pos), 1.25);
    });

    public static final ParticleSystem<ArtifactBladeAreaAttackData> ARTIFACT_BLADE_SMASH = CONTROLLER.register(ArtifactBladeAreaAttackData.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(35);
        ClientParticles.spawnPrecise(ParticleTypes.CRIT, world, data.targetPos.add(0, 1, 0), 5, 1, 5);

        for (var entityPos : data.entityPositions) {
            ClientParticles.setParticleCount(15);
            ClientParticles.spawnLine(
                    ParticleTypes.FIREWORK,
                    world,
                    data.targetPos.add(0, .15f, 0),
                    entityPos.add(0, .15f, 0),
                    .05f
            );

            ClientParticles.setParticleCount(3);
            ClientParticles.spawn(ParticleTypes.EXPLOSION, world, entityPos.add(0, 1, 0), 2.5);
        }
    });

    public static final ParticleSystem<Float> ARCANE_FADE_BLEACH_SHEEP = CONTROLLER.register(Float.class, (world, pos, scale) -> {
        ClientParticles.setParticleCount((int) (25 * scale));
        ClientParticles.spawn(ParticleTypes.WITCH, world, pos, scale);

        ClientParticles.setParticleCount(5);
        ClientParticles.spawn(scale > .5f ? ParticleTypes.POOF : new DustParticleEffect(new Vector3f(1), 1), world, pos, scale);
    });

    public static final ParticleSystem<Void> ARCANE_FADE_CRAFT = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(5);
        ClientParticles.spawn(ParticleTypes.CLOUD, world, pos, .25f);

        ClientParticles.randomizeVelocity(.15f);
        ClientParticles.setParticleCount(15);
        ClientParticles.spawn(ParticleTypes.FIREWORK, world, pos, .25f);
    });

    public static final ParticleSystem<Void> ASPEN_INFUSION_ACTIVE = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.spawn(
                new GenericEmitterParticleEffect(ParticleTypes.ENCHANT, new Vec3d(.2, .2, .2), 1, .25f, true, 10),
                world, pos, 0
        );
    });

    public static final ParticleSystem<Void> ASPEN_INFUSION_CRAFT = CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.randomizeVelocity(.15f);
        ClientParticles.setParticleCount(10);
        ClientParticles.spawn(ParticleTypes.FIREWORK, world, pos, .25f);

        ClientParticles.randomizeVelocity(.15f);
        ClientParticles.setParticleCount(15);
        ClientParticles.spawn(new DustColorTransitionParticleEffect(new Vector3f(.25f, .25f, 1f), new Vector3f(1f, .5f, .25f), 1f), world, pos.subtract(0, .1, 0), .35f);
    });

    public static final ParticleSystem<Integer> AETHUM_OVERCHARGE = CONTROLLER.register(Integer.class, (world, pos, entityId) -> {
        var entity = world.getEntityById(entityId);
        var client = MinecraftClient.getInstance();

        client.particleManager.addEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
        world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);

        if (entity == client.player) {
            ItemStack overcharger = null;
            for (var hand : Hand.values()) {
                if (!client.player.getStackInHand(hand).isOf(AffinityItems.AETHUM_OVERCHARGER)) continue;

                overcharger = client.player.getStackInHand(hand);
                break;
            }

            if (overcharger == null) return;
            client.gameRenderer.showFloatingItem(overcharger);
        }
    });

    // Context data types

    public record DissolveData(ItemStack suckWhat, Vec3d suckWhere, int duration, int particleMaxAge) {}

    public record LineData(Vec3d target, int color) {}

    public record BezierVortexData(ParticleEffect particle, List<Vec3d> originPositions, int emitDuration, int travelDuration, boolean randomPath) {}

    public record ArtifactBladeAreaAttackData(Vec3d targetPos, List<Vec3d> entityPositions) {}

    // ------------------

    public static void initialize() {}
}
