package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import io.wispforest.affinity.Affinity;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SacrificePetCriterion extends AbstractCriterion<SacrificePetCriterion.Conditions> {

    public static final Identifier ID = Affinity.id("sacrifice_pet");

    public void trigger(ServerPlayerEntity player, Entity pet) {
        var context = EntityPredicate.createAdvancementEntityLootContext(player, pet);
        this.trigger(player, conditions -> conditions.pet.test(context));
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate, LootContextPredicate.fromJson("pet", predicateDeserializer, obj.get("pet"), LootContextTypes.ADVANCEMENT_ENTITY));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static final class Conditions extends AbstractCriterionConditions {
        private final LootContextPredicate pet;

        public Conditions(LootContextPredicate player, LootContextPredicate pet) {
            super(ID, player);
            this.pet = pet;
        }
    }
}
