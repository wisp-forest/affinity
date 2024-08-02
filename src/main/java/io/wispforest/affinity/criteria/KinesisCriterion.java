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

public class KinesisCriterion extends AbstractCriterion<KinesisCriterion.Conditions> {

    public void trigger(ServerPlayerEntity player, Entity entity) {
        var context = EntityPredicate.createAdvancementEntityLootContext(player, entity);
        this.trigger(player, conditions -> conditions.entity.map(predicate -> predicate.test(context)).orElse(true));
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<LootContextPredicate> player,
                             Optional<LootContextPredicate> entity) implements AbstractCriterion.Conditions {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.optionalField("player", EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, false).forGetter(Conditions::player),
                Codec.optionalField("entity", EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, false).forGetter(Conditions::entity)
        ).apply(instance, Conditions::new));
    }
}
