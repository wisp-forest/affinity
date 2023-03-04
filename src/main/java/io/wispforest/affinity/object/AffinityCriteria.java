package io.wispforest.affinity.object;

import io.wispforest.affinity.criteria.*;
import io.wispforest.owo.registration.reflect.SimpleFieldProcessingSubject;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;

import java.lang.reflect.Field;

public class AffinityCriteria implements SimpleFieldProcessingSubject<Criterion<?>> {

    public static final ArtifactBladeSmashCriterion ARTIFACT_BLADE_SMASH = new ArtifactBladeSmashCriterion();
    public static final KinesisProjectileHitCriterion KINESIS_PROJECTILE_HIT = new KinesisProjectileHitCriterion();
    public static final BreakAethumFluxCacheCriterion BREAK_AETHUM_FLUX_CACHE = new BreakAethumFluxCacheCriterion();
    public static final KinesisCriterion KINESIS = new KinesisCriterion();
    public static final UsedOverchargerCriterion USED_OVERCHARGER = new UsedOverchargerCriterion();
    public static final SacrificedToRitualCriterion SACRIFICED_TO_RITUAL = new SacrificedToRitualCriterion();

    @Override
    public void processField(Criterion<?> value, String identifier, Field field) {
        Criteria.register(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Criterion<?>> getTargetFieldType() {
        return (Class<Criterion<?>>) (Object) Criterion.class;
    }
}
