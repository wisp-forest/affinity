package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.RecordEndec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.joml.Vector3f;

public record OrbitingEmitterParticleEffect(ParticleEffect outerEffect, ParticleEffect innerEffect, Vector3f speed,
                                            float radius, int emitInterval,
                                            int orbitSpeed, int lifetime) implements ParticleEffect {

    private static final Endec<OrbitingEmitterParticleEffect> ENDEC = RecordEndec.create(OrbitingEmitterParticleEffect.class);

    public static final ParticleEffect.Factory<OrbitingEmitterParticleEffect> FACTORY = new Factory<>() {
        @Override
        public OrbitingEmitterParticleEffect read(ParticleType<OrbitingEmitterParticleEffect> type, StringReader reader) {
            return new OrbitingEmitterParticleEffect(
                    new DustParticleEffect(MathUtil.rgbToVec3f(0x4C0033), .65f), new DustParticleEffect(MathUtil.rgbToVec3f(0xFEF5AC), .65f),
                    new Vector3f(.2f, 0, 0), .1f, 1, 25, 100
            );
        }

        @Override
        public OrbitingEmitterParticleEffect read(ParticleType<OrbitingEmitterParticleEffect> type, PacketByteBuf buf) {
            return buf.read(ENDEC);
        }
    };

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.ORBITING_EMITTER;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.write(ENDEC, this);
    }

    @Override
    public String asString() {
        return "spiraling emitter for {" + this.outerEffect.asString() + " and " + this.innerEffect.asString() + "}";
    }
}
