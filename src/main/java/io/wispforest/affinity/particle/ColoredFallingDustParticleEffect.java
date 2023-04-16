package io.wispforest.affinity.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.mixin.BlockFallingDustParticleInvoker;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.owo.util.VectorSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record ColoredFallingDustParticleEffect(Vector3f color) implements ParticleEffect {

    public static final Factory<ColoredFallingDustParticleEffect> FACTORY =
            new Factory<>() {
                @Override
                public ColoredFallingDustParticleEffect read(ParticleType<ColoredFallingDustParticleEffect> type, StringReader reader) throws CommandSyntaxException {
                    return new ColoredFallingDustParticleEffect(AbstractDustParticleEffect.readColor(reader));
                }

                @Override
                public ColoredFallingDustParticleEffect read(ParticleType<ColoredFallingDustParticleEffect> type, PacketByteBuf buf) {
                    return new ColoredFallingDustParticleEffect(VectorSerializer.readf(buf));
                }
            };

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.COLORED_FALLING_DUST;
    }

    @Override
    public void write(PacketByteBuf buf) {
        VectorSerializer.writef(buf, this.color);
    }

    @Override
    public String asString() {
        return "Colored Falling Dust";
    }

    @Environment(EnvType.CLIENT)
    public record ParticleFactory(
            SpriteProvider spriteProvider) implements net.minecraft.client.particle.ParticleFactory<ColoredFallingDustParticleEffect> {
        public @NotNull Particle createParticle(ColoredFallingDustParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return BlockFallingDustParticleInvoker.affinity$invokeNew(world, x, y, z, effect.color.x, effect.color.y, effect.color.z, this.spriteProvider);
        }
    }
}
