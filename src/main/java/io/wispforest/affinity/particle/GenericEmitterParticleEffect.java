package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.RecordEndec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

public record GenericEmitterParticleEffect(ParticleEffect effect, Vec3d emitVelocity, int emitInterval,
                                           float emitDeviation, boolean randomizeVelocity,
                                           int emitterLifetime) implements ParticleEffect {

    private static final Endec<GenericEmitterParticleEffect> ENDEC = RecordEndec.createShared(GenericEmitterParticleEffect.class);

    public static final ParticleEffect.Factory<GenericEmitterParticleEffect> FACTORY = new Factory<>() {
        @Override
        public GenericEmitterParticleEffect read(ParticleType<GenericEmitterParticleEffect> type, StringReader reader) {
            return new GenericEmitterParticleEffect(ParticleTypes.SMOKE, Vec3d.ZERO, 2, .15f, false, 20);
        }

        @Override
        public GenericEmitterParticleEffect read(ParticleType<GenericEmitterParticleEffect> type, PacketByteBuf buf) {
            return buf.read(ENDEC);
        }
    };

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.GENERIC_EMITTER;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.write(ENDEC, this);
    }

    @Override
    public String asString() {
        return "generic emitter for {" + this.effect.asString() + "}";
    }
}
