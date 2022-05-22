package io.wispforest.affinity.object;

import io.wispforest.affinity.client.particle.GenericEmitterParticle;
import io.wispforest.affinity.particle.BezierItemEmitterParticleEffect;
import io.wispforest.affinity.particle.BezierItemParticleEffect;
import io.wispforest.affinity.particle.ColoredFlameParticleEffect;
import io.wispforest.affinity.particle.GenericEmitterParticleEffect;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;

public class AffinityParticleTypes implements AutoRegistryContainer<ParticleType<?>> {

    public static final ParticleType<ColoredFlameParticleEffect> COLORED_FLAME = FabricParticleTypes.complex(ColoredFlameParticleEffect.FACTORY);
    public static final ParticleType<GenericEmitterParticleEffect> GENERIC_EMITTER = FabricParticleTypes.complex(GenericEmitterParticleEffect.FACTORY);

    public static final ParticleType<BezierItemParticleEffect> BEZIER_ITEM = FabricParticleTypes.complex(BezierItemParticleEffect.FACTORY);
    public static final ParticleType<BezierItemEmitterParticleEffect> BEZIER_ITEM_EMITTER = FabricParticleTypes.complex(BezierItemEmitterParticleEffect.FACTORY);


    @Override
    public Registry<ParticleType<?>> getRegistry() {
        return Registry.PARTICLE_TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ParticleType<?>> getTargetFieldType() {
        return (Class<ParticleType<?>>) (Object) ParticleType.class;
    }
}
