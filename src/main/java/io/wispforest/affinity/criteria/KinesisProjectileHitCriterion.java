package io.wispforest.affinity.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class KinesisProjectileHitCriterion extends AbstractCriterion<KinesisProjectileHitCriterion.Conditions> {

    public void trigger(ServerPlayerEntity player, Entity target, Entity projectile) {
        var targetContext = EntityPredicate.createAdvancementEntityLootContext(player, target);
        var projectileContext = EntityPredicate.createAdvancementEntityLootContext(player, projectile);

        this.trigger(player, conditions -> conditions.targetPredicate.map(predicate -> predicate.test(targetContext)).orElse(true)
                && conditions.projectilePredicate.map(predicate -> predicate.test(projectileContext)).orElse(true));
    }
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<LootContextPredicate> player,
                             Optional<LootContextPredicate> targetPredicate,
                             Optional<LootContextPredicate> projectilePredicate) implements AbstractCriterion.Conditions {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player").forGetter(Conditions::player),
                Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "target").forGetter(Conditions::targetPredicate),
                Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "projectile").forGetter(Conditions::projectilePredicate)
        ).apply(instance, Conditions::new));
    }
}
