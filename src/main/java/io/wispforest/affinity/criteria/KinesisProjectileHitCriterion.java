package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import io.wispforest.affinity.Affinity;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class KinesisProjectileHitCriterion extends AbstractCriterion<KinesisProjectileHitCriterion.Conditions> {

    public static final Identifier ID = Affinity.id("kinesis_projectile_hit");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        var targetPredicate = EntityPredicate.Extended.getInJson(obj, "target", predicateDeserializer);
        var projectilePredicate = EntityPredicate.Extended.getInJson(obj, "projectile", predicateDeserializer);
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

        private final EntityPredicate.Extended targetPredicate;
        private final EntityPredicate.Extended projectilePredicate;

        public Conditions(EntityPredicate.Extended player, EntityPredicate.Extended targetPredicate, EntityPredicate.Extended projectilePredicate) {
            super(ID, player);
            this.targetPredicate = targetPredicate;
            this.projectilePredicate = projectilePredicate;
        }

    }

}
