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

import java.util.function.BiFunction;

public class BezierItemParticleEffect implements ParticleEffect {

    private final ItemStack stack;
    private final Vec3d splineEndpoint;

    public BezierItemParticleEffect(ItemStack stack, Vec3d splineEndpoint) {
        this.stack = stack;
        this.splineEndpoint = splineEndpoint;
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

    public static <T extends BezierItemParticleEffect> Factory<T> makeFactory(BiFunction<ItemStack, Vec3d, T> instanceCreator) {
        return new Factory<>() {
            @Override
            public T read(ParticleType<T> type, StringReader reader) throws CommandSyntaxException {
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

                return instanceCreator.apply(stack, new Vec3d(x, y, z));
            }

            @Override
            public T read(ParticleType<T> type, PacketByteBuf buf) {
                return instanceCreator.apply(buf.readItemStack(), VectorSerializer.read(buf));
            }
        };
    }

}
