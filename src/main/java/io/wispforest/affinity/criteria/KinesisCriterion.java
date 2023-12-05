package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class KinesisCriterion extends AbstractCriterion<KinesisCriterion.Conditions> {

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate, LootContextPredicate.fromJson("entity", predicateDeserializer, obj.get("entity"), LootContextTypes.ADVANCEMENT_ENTITY).orElseGet(Optional::empty));
    }

    public void trigger(ServerPlayerEntity player, Entity entity) {
        var context = EntityPredicate.createAdvancementEntityLootContext(player, entity);
        this.trigger(player, conditions -> conditions.entity.map(predicate -> predicate.test(context)).orElse(true));
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final Optional<LootContextPredicate> entity;

        public Conditions(Optional<LootContextPredicate> player, Optional<LootContextPredicate> entity) {
            super(player);
            this.entity = entity;
        }
    }

}
