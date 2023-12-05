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

public class KinesisProjectileHitCriterion extends AbstractCriterion<KinesisProjectileHitCriterion.Conditions> {

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        var targetPredicate = LootContextPredicate.fromJson("target", predicateDeserializer, obj, LootContextTypes.ADVANCEMENT_ENTITY);
        var projectilePredicate = LootContextPredicate.fromJson("projectile", predicateDeserializer, obj, LootContextTypes.ADVANCEMENT_ENTITY);
        return new Conditions(playerPredicate, targetPredicate.orElseGet(Optional::empty), projectilePredicate.orElseGet(Optional::empty));
    }

    public void trigger(ServerPlayerEntity player, Entity target, Entity projectile) {
        var targetContext = EntityPredicate.createAdvancementEntityLootContext(player, target);
        var projectileContext = EntityPredicate.createAdvancementEntityLootContext(player, projectile);

        this.trigger(player, conditions -> conditions.targetPredicate.map(predicate -> predicate.test(targetContext)).orElse(true)
                && conditions.projectilePredicate.map(predicate -> predicate.test(projectileContext)).orElse(true));
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final Optional<LootContextPredicate> targetPredicate;
        private final Optional<LootContextPredicate> projectilePredicate;

        public Conditions(Optional<LootContextPredicate> player, Optional<LootContextPredicate> targetPredicate, Optional<LootContextPredicate> projectilePredicate) {
            super(player);
            this.targetPredicate = targetPredicate;
            this.projectilePredicate = projectilePredicate;
        }

    }

}
