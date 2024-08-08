package io.wispforest.affinity.particle;

import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public record GenericEmitterParticleEffect(ParticleEffect effect, Vec3d emitVelocity, int emitInterval,
                                           float emitDeviation, boolean randomizeVelocity,
                                           int emitterLifetime) implements ParticleEffect {

    public static final StructEndec<GenericEmitterParticleEffect> ENDEC = RecordEndec.create(new ReflectiveEndecBuilder(GenericEmitterParticleEffect::addEndecs), GenericEmitterParticleEffect.class);

    private static void addEndecs(ReflectiveEndecBuilder builder) {
        MinecraftEndecs.addDefaults(builder);
        AffinityNetwork.addEndecs(builder);
    }

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.GENERIC_EMITTER;
    }
}
