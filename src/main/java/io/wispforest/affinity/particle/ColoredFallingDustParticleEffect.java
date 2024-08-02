package io.wispforest.affinity.particle;

import io.wispforest.affinity.mixin.client.BlockFallingDustParticleInvoker;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record ColoredFallingDustParticleEffect(Vector3f color) implements ParticleEffect {

    public static final StructEndec<ColoredFallingDustParticleEffect> ENDEC = RecordEndec.create(
            new ReflectiveEndecBuilder(MinecraftEndecs::addDefaults),
            ColoredFallingDustParticleEffect.class
    );

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.COLORED_FALLING_DUST;
    }

    @Environment(EnvType.CLIENT)
    public record ParticleFactory(
            SpriteProvider spriteProvider) implements net.minecraft.client.particle.ParticleFactory<ColoredFallingDustParticleEffect> {
        public @NotNull Particle createParticle(ColoredFallingDustParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return BlockFallingDustParticleInvoker.affinity$invokeNew(world, x, y, z, effect.color.x, effect.color.y, effect.color.z, this.spriteProvider);
        }
    }
}
