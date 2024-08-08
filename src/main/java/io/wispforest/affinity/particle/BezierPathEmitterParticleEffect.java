package io.wispforest.affinity.particle;

import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

public record BezierPathEmitterParticleEffect(ParticleEffect effect, Vec3d splineEndpoint, int travelDuration,
                                              int emitterDuration,
                                              boolean randomPath) implements ParticleEffect {

    public static final StructEndec<BezierPathEmitterParticleEffect> ENDEC = RecordEndec.create(new ReflectiveEndecBuilder(BezierPathEmitterParticleEffect::addEndecs), BezierPathEmitterParticleEffect.class);

    private static void addEndecs(ReflectiveEndecBuilder builder) {
        MinecraftEndecs.addDefaults(builder);
        AffinityNetwork.addEndecs(builder);
    }

    public static BezierPathEmitterParticleEffect item(ItemStack stack, Vec3d splineEndpoint, int travelDuration, int emitterDuration, boolean randomPath) {
        return new BezierPathEmitterParticleEffect(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), splineEndpoint, travelDuration, emitterDuration, randomPath);
    }

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.BEZIER_PATH_EMITTER;
    }
}
