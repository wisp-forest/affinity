package io.wispforest.affinity.particle;

import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.RecordEndec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record ColoredFlameParticleEffect(int dyeColorId) implements ParticleEffect {

    public static final StructEndec<ColoredFlameParticleEffect> ENDEC = RecordEndec.createShared(ColoredFlameParticleEffect.class);

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.COLORED_FLAME;
    }
}
