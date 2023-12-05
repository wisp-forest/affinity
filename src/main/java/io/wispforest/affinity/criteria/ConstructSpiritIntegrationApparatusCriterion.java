package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class ConstructSpiritIntegrationApparatusCriterion extends AbstractCriterion<ConstructSpiritIntegrationApparatusCriterion.Conditions> {

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate, NumberRange.IntRange.fromJson(obj.get("completion")));
    }

    public void trigger(ServerPlayerEntity player, int completion) {
        this.trigger(player, conditions -> conditions.completion.test(completion));
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final NumberRange.IntRange completion;

        public Conditions(Optional<LootContextPredicate> entity, NumberRange.IntRange completion) {
            super(entity);
            this.completion = completion;
        }
    }
}
