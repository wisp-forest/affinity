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

public class ConstructSpiritIntegrationApparatusCriterion extends AbstractCriterion<ConstructSpiritIntegrationApparatusCriterion.Conditions> {

    public static final Identifier ID = Affinity.id("construct_spirit_integration_apparatus");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate, NumberRange.IntRange.fromJson(obj.get("completion")));
    }

    public void trigger(ServerPlayerEntity player, int completion) {
        this.trigger(player, conditions -> conditions.completion.test(completion));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final NumberRange.IntRange completion;

        public Conditions(EntityPredicate.Extended entity, NumberRange.IntRange completion) {
            super(ID, entity);
            this.completion = completion;
        }
    }
}
