package io.wispforest.affinity.particle;

import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.joml.Vector3f;

public record OrbitingEmitterParticleEffect(ParticleEffect outerEffect, ParticleEffect innerEffect, Vector3f speed,
                                            float radius, int emitInterval,
                                            int orbitSpeed, int lifetime) implements ParticleEffect {

    public static final StructEndec<OrbitingEmitterParticleEffect> ENDEC = RecordEndec.create(new ReflectiveEndecBuilder(MinecraftEndecs::addDefaults), OrbitingEmitterParticleEffect.class);

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.ORBITING_EMITTER;
    }
}
