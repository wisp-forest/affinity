package io.wispforest.affinity.endec;

import io.wispforest.affinity.endec.nbt.NbtEndec;
import io.wispforest.endec.*;
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
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.PACKET_BYTE_BUF, PacketByteBuf.class);

        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.BLOCK_POS, BlockPos.class);
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.CHUNK_POS, ChunkPos.class);
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.ITEM_STACK, ItemStack.class);
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.IDENTIFIER, Identifier.class);
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(NbtEndec.COMPOUND, NbtCompound.class);
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(
                new StructEndec<>() {
                    final Endec<Direction> DIRECTION = Endec.forEnum(Direction.class);

                    @Override
                    public void encodeStruct(SerializationContext ctx, Serializer serializer, Serializer.Struct struct, BlockHitResult hitResult) {
                        BlockPos blockPos = hitResult.getBlockPos();
                        struct.field("blockPos", ctx, BuiltInEndecs.BLOCK_POS, blockPos)
                                .field("side", ctx, DIRECTION, hitResult.getSide());

                        Vec3d vec3d = hitResult.getPos();
                        struct.field("x", ctx, Endec.FLOAT, (float) (vec3d.x - (double) blockPos.getX()))
                                .field("y", ctx, Endec.FLOAT, (float) (vec3d.x - (double) blockPos.getX()))
                                .field("z", ctx, Endec.FLOAT, (float) (vec3d.x - (double) blockPos.getX()))
                                .field("inside", ctx, Endec.BOOLEAN, hitResult.isInsideBlock());
                    }

                    @Override
                    public BlockHitResult decodeStruct(SerializationContext ctx, Deserializer deserializer, Deserializer.Struct struct) {
                        BlockPos blockPos = struct.field("blockPos", ctx, BuiltInEndecs.BLOCK_POS);
                        Direction direction = struct.field("side", ctx, DIRECTION);

                        float f = struct.field("x", ctx, Endec.FLOAT);
                        float g = struct.field("y", ctx, Endec.FLOAT);
                        float h = struct.field("z", ctx, Endec.FLOAT);

                        boolean bl = struct.field("inside", ctx, Endec.BOOLEAN);
                        return new BlockHitResult(
                                new Vec3d((double) blockPos.getX() + (double) f, (double) blockPos.getY() + (double) g, (double) blockPos.getZ() + (double) h), direction, blockPos, bl
                        );
                    }
                },
                BlockHitResult.class
        );

        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.TEXT, Text.class);
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.PACKET_BYTE_BUF.xmap(
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

        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.VEC3D, Vec3d.class);
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.VECTOR3F, Vector3f.class);
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(BuiltInEndecs.VEC3I, Vec3i.class);
    }
}
