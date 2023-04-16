package io.wispforest.affinity.object;

import io.wispforest.affinity.particle.*;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class AffinityParticleTypes implements AutoRegistryContainer<ParticleType<?>> {

    public static final ParticleType<ColoredFlameParticleEffect> COLORED_FLAME = FabricParticleTypes.complex(ColoredFlameParticleEffect.FACTORY);
    public static final ParticleType<SmallColoredFlameParticleEffect> SMALL_COLORED_FLAME = FabricParticleTypes.complex(SmallColoredFlameParticleEffect.FACTORY);
    public static final ParticleType<GenericEmitterParticleEffect> GENERIC_EMITTER = FabricParticleTypes.complex(GenericEmitterParticleEffect.FACTORY);
    public static final ParticleType<OrbitingEmitterParticleEffect> ORBITING_EMITTER = FabricParticleTypes.complex(OrbitingEmitterParticleEffect.FACTORY);
    public static final ParticleType<ColoredFallingDustParticleEffect> COLORED_FALLING_DUST = FabricParticleTypes.complex(ColoredFallingDustParticleEffect.FACTORY);

    public static final ParticleType<BezierPathParticleEffect> BEZIER_PATH = FabricParticleTypes.complex(BezierPathParticleEffect.FACTORY);
    public static final ParticleType<BezierPathEmitterParticleEffect> BEZIER_PATH_EMITTER = FabricParticleTypes.complex(BezierPathEmitterParticleEffect.FACTORY);

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
