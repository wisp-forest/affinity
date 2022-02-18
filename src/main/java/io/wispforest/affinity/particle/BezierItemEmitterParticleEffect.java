package io.wispforest.affinity.particle;

import io.wispforest.affinity.object.AffinityParticleTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public class BezierItemEmitterParticleEffect extends BezierItemParticleEffect {

    public BezierItemEmitterParticleEffect(ItemStack stack, Vec3d splineEndpoint) {
        super(stack, splineEndpoint);
    }

    @Override
    public ParticleType<?> getType() {
        return AffinityParticleTypes.BEZIER_ITEM_EMITTER;
    }
}
