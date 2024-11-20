package io.wispforest.affinity.object;

import io.wispforest.affinity.particle.*;
import io.wispforest.endec.StructEndec;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class AffinityParticleTypes implements AutoRegistryContainer<ParticleType<?>> {

    public static final ParticleType<ColoredFlameParticleEffect> COLORED_FLAME = createType(ColoredFlameParticleEffect.ENDEC);
    public static final ParticleType<SmallColoredFlameParticleEffect> SMALL_COLORED_FLAME = createType(SmallColoredFlameParticleEffect.ENDEC);
    public static final ParticleType<GenericEmitterParticleEffect> GENERIC_EMITTER = createType(GenericEmitterParticleEffect.ENDEC);
    public static final ParticleType<OrbitingEmitterParticleEffect> ORBITING_EMITTER = createType(OrbitingEmitterParticleEffect.ENDEC);
    public static final ParticleType<ColoredFallingDustParticleEffect> COLORED_FALLING_DUST = createType(ColoredFallingDustParticleEffect.ENDEC);

    public static final ParticleType<BezierPathParticleEffect> BEZIER_PATH = createType(BezierPathParticleEffect.ENDEC);
    public static final ParticleType<BezierPathEmitterParticleEffect> BEZIER_PATH_EMITTER = createType(BezierPathEmitterParticleEffect.ENDEC);

    public static final ParticleType<DirectionalShriekParticleEffect> DIRECTIONAL_SHRIEK = createType(DirectionalShriekParticleEffect.ENDEC);

    private static <T extends ParticleEffect> ParticleType<T> createType(StructEndec<T> endec) {
        return FabricParticleTypes.complex(CodecUtils.toMapCodec(endec), CodecUtils.toPacketCodec(endec));
    }

    @Override
    public Registry<ParticleType<?>> getRegistry() {
        return Registries.PARTICLE_TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ParticleType<?>> getTargetFieldType() {
        return (Class<ParticleType<?>>) (Object) ParticleType.class;
    }
}
