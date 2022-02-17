package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.owo.network.serialization.RecordSerializer;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public record BezierItemParticleEffect(ItemStack stack, Vec3d gravityCenter) implements ParticleEffect {

    private static final RecordSerializer<BezierItemParticleEffect> SERIALIZER = RecordSerializer.create(BezierItemParticleEffect.class);

    public static final Factory<BezierItemParticleEffect> FACTORY =
            new Factory<>() {
                @Override
                public BezierItemParticleEffect read(ParticleType<BezierItemParticleEffect> type, StringReader reader) throws CommandSyntaxException {
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

                    return new BezierItemParticleEffect(stack, new Vec3d(x, y, z));
                }

                @Override
                public BezierItemParticleEffect read(ParticleType<BezierItemParticleEffect> type, PacketByteBuf buf) {
                    return SERIALIZER.read(buf);
                }
            };

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.BEZIER_ITEM;
    }

    @Override
    public void write(PacketByteBuf buf) {
        SERIALIZER.write(buf, this);
    }

    @Override
    public String asString() {
        return "orbiting item";
    }
}
