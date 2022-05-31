package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.owo.network.serialization.RecordSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public record BezierPathParticleEffect(ParticleEffect effect, Vec3d splineEndpoint, int travelDuration) implements ParticleEffect {

    private static final RecordSerializer<BezierPathParticleEffect> SERIALIZER = RecordSerializer.create(BezierPathParticleEffect.class);

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.BEZIER_PATH;
    }

    @Override
    public void write(PacketByteBuf buf) {
        SERIALIZER.write(buf, this);
    }

    @Override
    public String asString() {
        return String.valueOf(Registry.PARTICLE_TYPE.getId(this.getType()));
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

            return new BezierPathParticleEffect(ParticleTypes.WHITE_ASH, new Vec3d(x, y, z), travelDuration);
        }

        @Override
        public BezierPathParticleEffect read(ParticleType<BezierPathParticleEffect> type, PacketByteBuf buf) {
            return SERIALIZER.read(buf);
        }
    };
}
