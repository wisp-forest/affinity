package io.wispforest.affinity.particle;

import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityParticleTypes;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public record BezierPathParticleEffect(ParticleEffect effect, Vec3d splineEndpoint, int travelDuration,
                                       boolean randomPath) implements ParticleEffect {

    public static final StructEndec<BezierPathParticleEffect> ENDEC = RecordEndec.create(new ReflectiveEndecBuilder(BezierPathParticleEffect::addEndecs), BezierPathParticleEffect.class);

    private static void addEndecs(ReflectiveEndecBuilder builder) {
        MinecraftEndecs.addDefaults(builder);
        AffinityNetwork.addEndecs(builder);
    }

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.BEZIER_PATH;
    }

}
