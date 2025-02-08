package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.endec.CodecUtils;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.util.VectorSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.AbstractDustParticleEffect;
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

    @Override
    public void write(PacketByteBuf buf) {
        buf.write(ENDEC, this);
    }

    @Override
    public String asString() {
        return "";
    }

    public static final Factory<DirectionalShriekParticleEffect> FACTORY =
        new Factory<>() {
            @Override
            public DirectionalShriekParticleEffect read(ParticleType<DirectionalShriekParticleEffect> type, StringReader reader) throws CommandSyntaxException {
                return new DirectionalShriekParticleEffect(0, Direction.UP);
            }

            @Override
            public DirectionalShriekParticleEffect read(ParticleType<DirectionalShriekParticleEffect> type, PacketByteBuf buf) {
                return buf.read(ENDEC);
            }
        };
}