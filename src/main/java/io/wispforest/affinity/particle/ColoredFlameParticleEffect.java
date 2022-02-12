package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.wispforest.affinity.object.AffinityParticleTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public record ColoredFlameParticleEffect(int dyeColorId) implements ParticleEffect {

    private static final SimpleCommandExceptionType NO_COLOR = new SimpleCommandExceptionType(Text.of("Invalid color"));

    public ColoredFlameParticleEffect(DyeColor color) {
        this(color.getId());
    }

    public static final ParticleEffect.Factory<ColoredFlameParticleEffect> FACTORY =
            new Factory<>() {
                @Override
                public ColoredFlameParticleEffect read(ParticleType<ColoredFlameParticleEffect> type, StringReader reader) throws CommandSyntaxException {
                    reader.expect(' ');
                    var color = reader.readString();

                    var dyeColor = DyeColor.byName(color, null);
                    if (dyeColor == null) throw NO_COLOR.create();

                    return new ColoredFlameParticleEffect(dyeColor);
                }

                @Override
                public ColoredFlameParticleEffect read(ParticleType<ColoredFlameParticleEffect> type, PacketByteBuf buf) {
                    return new ColoredFlameParticleEffect(buf.readByte());
                }
            };

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.COLORED_FLAME;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(this.dyeColorId());
    }

    @Override
    public String asString() {
        return "colored flame";
    }
}
