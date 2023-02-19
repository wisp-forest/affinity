package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import io.wispforest.affinity.Affinity;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ArtifactBladeSmashCriterion extends AbstractCriterion<ArtifactBladeSmashCriterion.Conditions> {

    public static final Identifier ID = Affinity.id("artifact_blade_smash");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate, NumberRange.IntRange.fromJson(obj.get("fall_distance")));
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, conditions -> conditions.fallDistance.test((int) player.fallDistance));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final NumberRange.IntRange fallDistance;

        public Conditions(EntityPredicate.Extended player, NumberRange.IntRange fallDistance) {
            super(ID, player);
            this.fallDistance = fallDistance;
        }
    }

}
