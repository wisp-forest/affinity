package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;

import java.util.Optional;

public class MinedPeculiarClumpCriterion extends AbstractCriterion<MinedPeculiarClumpCriterion.Conditions> {

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate, JsonHelper.getBoolean(obj, "expect_success", false));
    }

    public void trigger(ServerPlayerEntity player, boolean wasMinedCorrectly) {
        this.trigger(player, conditions -> conditions.expectSuccess == wasMinedCorrectly);
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final boolean expectSuccess;

        public Conditions(Optional<LootContextPredicate> player, boolean expectSuccess) {
            super(player);
            this.expectSuccess = expectSuccess;
        }
    }

}
