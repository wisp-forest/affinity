package io.wispforest.affinity.mixin.endec;

import io.wispforest.endec.Endec;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.endec.util.EndecBuffer;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin implements EndecBuffer {
    @Override
    public <T> void write(Endec<T> endec, T value) {
        endec.encodeFully(() -> ByteBufSerializer.of((PacketByteBuf) (Object) this), value);
    }

    @Override
    public <T> T read(Endec<T> endec) {
        return endec.decodeFully(ByteBufDeserializer::of, (PacketByteBuf) (Object) this);
    }
}
