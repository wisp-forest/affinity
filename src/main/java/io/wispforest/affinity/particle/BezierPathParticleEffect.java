package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.RecordEndec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Vec3d;

public record BezierPathParticleEffect(ParticleEffect effect, Vec3d splineEndpoint, int travelDuration,
                                       boolean randomPath) implements ParticleEffect {

    private static final Endec<BezierPathParticleEffect> ENDEC = RecordEndec.create(BezierPathParticleEffect.class);

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.BEZIER_PATH;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.write(ENDEC, this);
    }

    @Override
    public String asString() {
        return String.valueOf(Registries.PARTICLE_TYPE.getId(this.getType()));
    }

    public static final Factory<BezierPathParticleEffect> FACTORY = new Factory<>() {
        @Override
        public BezierPathParticleEffect read(ParticleType<BezierPathParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            var x = reader.readDouble();

            reader.expect(' ');
            var y = reader.readDouble();

            reader.expect(' ');
            var z = reader.readDouble();

            reader.expect(' ');
            var travelDuration = reader.readInt();

            reader.expect(' ');
            var randomPath = reader.readBoolean();

            return new BezierPathParticleEffect(ParticleTypes.WHITE_ASH, new Vec3d(x, y, z), travelDuration, randomPath);
        }

        @Override
        public BezierPathParticleEffect read(ParticleType<BezierPathParticleEffect> type, PacketByteBuf buf) {
            return buf.read(ENDEC);
        }
    };
}
