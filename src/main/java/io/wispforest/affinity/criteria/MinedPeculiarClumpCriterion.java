package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import io.wispforest.affinity.Affinity;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class MinedPeculiarClumpCriterion extends AbstractCriterion<MinedPeculiarClumpCriterion.Conditions> {

    public static final Identifier ID = Affinity.id("mined_peculiar_clump");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate, JsonHelper.getBoolean(obj, "expect_success", false));
    }

    public void trigger(ServerPlayerEntity player, boolean wasMinedCorrectly) {
        this.trigger(player, conditions -> conditions.expectSuccess == wasMinedCorrectly);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final boolean expectSuccess;

        public Conditions(EntityPredicate.Extended player, boolean expectSuccess) {
            super(ID, player);
            this.expectSuccess = expectSuccess;
        }
    }

}
