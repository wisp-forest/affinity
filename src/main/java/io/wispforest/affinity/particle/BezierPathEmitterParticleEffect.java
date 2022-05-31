package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.owo.network.serialization.RecordSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

public record BezierPathEmitterParticleEffect(ParticleEffect effect, Vec3d splineEndpoint, int travelDuration, int emitterDuration) implements ParticleEffect {

    private static final RecordSerializer<BezierPathEmitterParticleEffect> SERIALIZER = RecordSerializer.create(BezierPathEmitterParticleEffect.class);

    public static BezierPathEmitterParticleEffect item(ItemStack stack, Vec3d splineEndpoint, int travelDuration, int emitterDuration) {
        return new BezierPathEmitterParticleEffect(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), splineEndpoint, travelDuration, emitterDuration);
    }

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.BEZIER_PATH_EMITTER;
    }

    @Override
    public void write(PacketByteBuf buf) {
        SERIALIZER.write(buf, this);
    }

    @Override
    public String asString() {
        return "yes this is an emitter i'm lazy";
    }

    public static final Factory<BezierPathEmitterParticleEffect> FACTORY = new Factory<>() {
        @Override
        public BezierPathEmitterParticleEffect read(ParticleType<BezierPathEmitterParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            var x = reader.readDouble();

            reader.expect(' ');
            var y = reader.readDouble();

            reader.expect(' ');
            var z = reader.readDouble();

            reader.expect(' ');
            var emitterDuration = reader.readInt();

            reader.expect(' ');
            var travelDuration = reader.readInt();

            return new BezierPathEmitterParticleEffect(ParticleTypes.WHITE_ASH, new Vec3d(x, y, z), emitterDuration, travelDuration);
        }

        @Override
        public BezierPathEmitterParticleEffect read(ParticleType<BezierPathEmitterParticleEffect> type, PacketByteBuf buf) {
            return SERIALIZER.read(buf);
        }
    };
}
