package io.wispforest.affinity.endec;

import io.wispforest.affinity.endec.nbt.NbtEndec;
import io.wispforest.endec.Deserializer;
import io.wispforest.endec.Endec;
import io.wispforest.endec.Serializer;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import org.joml.Vector3f;

public class ReflectiveMinecraftEndecs {

    public static void init() {
        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.PACKET_BYTE_BUF, PacketByteBuf.class);

        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.BLOCK_POS, BlockPos.class);
        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.CHUNK_POS, ChunkPos.class);
        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.ITEM_STACK, ItemStack.class);
        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.IDENTIFIER, Identifier.class);
        ReflectiveEndecBuilder.INSTANCE.register(NbtEndec.COMPOUND, NbtCompound.class);
        ReflectiveEndecBuilder.INSTANCE.register(
                new StructEndec<>() {
                    final Endec<Direction> DIRECTION = Endec.forEnum(Direction.class);

                    @Override
                    public void encodeStruct(Serializer serializer,  Serializer.Struct struct, BlockHitResult hitResult) {
                        BlockPos blockPos = hitResult.getBlockPos();
                        struct.field("blockPos", BuiltInEndecs.BLOCK_POS, blockPos)
                                .field("side", DIRECTION, hitResult.getSide());

                        Vec3d vec3d = hitResult.getPos();
                        struct.field("x", Endec.FLOAT, (float) (vec3d.x - (double) blockPos.getX()))
                                .field("y", Endec.FLOAT, (float) (vec3d.x - (double) blockPos.getX()))
                                .field("z", Endec.FLOAT, (float) (vec3d.x - (double) blockPos.getX()))
                                .field("inside", Endec.BOOLEAN, hitResult.isInsideBlock());
                    }

                    @Override
                    public BlockHitResult decodeStruct(Deserializer deserializer, Deserializer.Struct struct) {
                        BlockPos blockPos = struct.field("blockPos", BuiltInEndecs.BLOCK_POS);
                        Direction direction = struct.field("side", DIRECTION);

                        float f = struct.field("x", Endec.FLOAT);
                        float g = struct.field("y", Endec.FLOAT);
                        float h = struct.field("z", Endec.FLOAT);

                        boolean bl = struct.field("inside", Endec.BOOLEAN);
                        return new BlockHitResult(
                                new Vec3d((double) blockPos.getX() + (double) f, (double) blockPos.getY() + (double) g, (double) blockPos.getZ() + (double) h), direction, blockPos, bl
                        );
                    }
                },
                BlockHitResult.class
        );

        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.TEXT, Text.class);
        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.PACKET_BYTE_BUF.xmap(
                byteBuf -> {
                    //noinspection rawtypes
                    final ParticleType particleType = Registries.PARTICLE_TYPE.get(byteBuf.readInt());
                    //noinspection unchecked, ConstantConditions

                    return particleType.getParametersFactory().read(particleType, byteBuf);
                },
                particleEffect -> {
                    var buf = PacketByteBufs.create();

                    buf.writeInt(Registries.PARTICLE_TYPE.getRawId(particleEffect.getType()));
                    particleEffect.write(buf);

                    return buf;
                }
        ), ParticleEffect.class);

        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.VEC3D, Vec3d.class);
        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.VECTOR3F, Vector3f.class);
        ReflectiveEndecBuilder.INSTANCE.register(BuiltInEndecs.VEC3I, Vec3i.class);
    }
}
