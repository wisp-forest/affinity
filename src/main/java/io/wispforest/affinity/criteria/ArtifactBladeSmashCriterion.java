package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import io.wispforest.affinity.Affinity;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ArtifactBladeSmashCriterion extends AbstractCriterion<ArtifactBladeSmashCriterion.Conditions> {

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> predicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(predicate, NumberRange.IntRange.fromJson(obj.get("fall_distance")));
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, conditions -> conditions.fallDistance.test((int) player.fallDistance));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final NumberRange.IntRange fallDistance;

        public Conditions(Optional<LootContextPredicate> player, NumberRange.IntRange fallDistance) {
            super(player);
            this.fallDistance = fallDistance;
        }
    }

}
