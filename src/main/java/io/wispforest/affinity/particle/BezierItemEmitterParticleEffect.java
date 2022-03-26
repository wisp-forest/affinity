package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.owo.util.VectorSerializer;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public class BezierItemEmitterParticleEffect extends BezierItemParticleEffect {

    private final int emitterDuration;

    public BezierItemEmitterParticleEffect(ItemStack stack, Vec3d splineEndpoint, int travelDuration, int emitterDuration) {
        super(stack, splineEndpoint, travelDuration);
        this.emitterDuration = emitterDuration;
    }

    public int emitterDuration() {
        return emitterDuration;
    }

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.BEZIER_ITEM_EMITTER;
    }

    public static final Factory<BezierItemEmitterParticleEffect> FACTORY = new Factory<>() {
        @Override
        public BezierItemEmitterParticleEffect read(ParticleType<BezierItemEmitterParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            var itemStringReader = new ItemStringReader(reader, false).consume();
            var stack = new ItemStackArgument(itemStringReader.getItem(), itemStringReader.getNbt())
                    .createStack(1, false);

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

            return new BezierItemEmitterParticleEffect(stack, new Vec3d(x, y, z), emitterDuration, travelDuration);
        }

        @Override
        public BezierItemEmitterParticleEffect read(ParticleType<BezierItemEmitterParticleEffect> type, PacketByteBuf buf) {
            return new BezierItemEmitterParticleEffect(buf.readItemStack(), VectorSerializer.read(buf), buf.readVarInt(), buf.readVarInt());
        }
    };
}
