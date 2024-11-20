package io.wispforest.affinity.particle;

import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Direction;

public record DirectionalShriekParticleEffect(int delay, Direction direction) implements ParticleEffect {

    public static final StructEndec<DirectionalShriekParticleEffect> ENDEC = StructEndecBuilder.of(
        Endec.INT.fieldOf("delay", DirectionalShriekParticleEffect::delay),
        CodecUtils.toEndec(net.minecraft.util.math.Direction.CODEC).fieldOf("direction", DirectionalShriekParticleEffect::direction),
        DirectionalShriekParticleEffect::new
    );

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.DIRECTIONAL_SHRIEK;
    }
}
