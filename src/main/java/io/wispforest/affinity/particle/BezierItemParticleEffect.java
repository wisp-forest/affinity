package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.owo.util.VectorSerializer;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class BezierItemParticleEffect implements ParticleEffect {

    private final ItemStack stack;
    private final Vec3d splineEndpoint;
    private final int travelDuration;

    public BezierItemParticleEffect(ItemStack stack, Vec3d splineEndpoint, int travelDuration) {
        this.stack = stack;
        this.splineEndpoint = splineEndpoint;
        this.travelDuration = travelDuration;
    }

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.BEZIER_ITEM;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeItemStack(this.stack);
        VectorSerializer.write(this.splineEndpoint, buf);
    }

    @Override
    public String asString() {
        return String.valueOf(Registry.PARTICLE_TYPE.getId(this.getType()));
    }

    public ItemStack stack() {
        return stack;
    }

    public Vec3d splineEndpoint() {
        return splineEndpoint;
    }

    public int travelDuration() {
        return this.travelDuration;
    }

    public static final Factory<BezierItemParticleEffect> FACTORY = new Factory<>() {
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

            reader.expect(' ');
            var travelDuration = reader.readInt();

            return new BezierItemParticleEffect(stack, new Vec3d(x, y, z), travelDuration);
        }

        @Override
        public BezierItemParticleEffect read(ParticleType<BezierItemParticleEffect> type, PacketByteBuf buf) {
            return new BezierItemParticleEffect(buf.readItemStack(), VectorSerializer.read(buf), buf.readVarInt());
        }
    };
}
