package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class SacrificedToRitualCriterion extends AbstractCriterion<SacrificedToRitualCriterion.Conditions> {

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate);
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, conditions -> true);
    }

    public static class Conditions extends AbstractCriterionConditions {
        public Conditions(Optional<LootContextPredicate> player) {
            super(player);
        }
    }

}
