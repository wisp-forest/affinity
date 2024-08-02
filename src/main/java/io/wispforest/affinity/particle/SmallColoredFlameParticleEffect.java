package io.wispforest.affinity.particle;

import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.RecordEndec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record SmallColoredFlameParticleEffect(int dyeColorId) implements ParticleEffect {

    public static final StructEndec<SmallColoredFlameParticleEffect> ENDEC = RecordEndec.createShared(SmallColoredFlameParticleEffect.class);

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.SMALL_COLORED_FLAME;
    }
}
