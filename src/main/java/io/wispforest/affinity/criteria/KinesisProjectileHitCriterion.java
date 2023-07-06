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

public class KinesisProjectileHitCriterion extends AbstractCriterion<KinesisProjectileHitCriterion.Conditions> {

    public static final Identifier ID = Affinity.id("kinesis_projectile_hit");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        var targetPredicate = LootContextPredicate.fromJson("target", predicateDeserializer, obj, LootContextTypes.ADVANCEMENT_ENTITY);
        var projectilePredicate = LootContextPredicate.fromJson("projectile", predicateDeserializer, obj, LootContextTypes.ADVANCEMENT_ENTITY);
        return new Conditions(playerPredicate, targetPredicate, projectilePredicate);
    }

    public void trigger(ServerPlayerEntity player, Entity target, Entity projectile) {
        var targetContext = EntityPredicate.createAdvancementEntityLootContext(player, target);
        var projectileContext = EntityPredicate.createAdvancementEntityLootContext(player, projectile);
        this.trigger(player, conditions -> conditions.targetPredicate.test(targetContext) && conditions.projectilePredicate.test(projectileContext));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final LootContextPredicate targetPredicate;
        private final LootContextPredicate projectilePredicate;

        public Conditions(LootContextPredicate player, LootContextPredicate targetPredicate, LootContextPredicate projectilePredicate) {
            super(ID, player);
            this.targetPredicate = targetPredicate;
            this.projectilePredicate = projectilePredicate;
        }

    }

}
