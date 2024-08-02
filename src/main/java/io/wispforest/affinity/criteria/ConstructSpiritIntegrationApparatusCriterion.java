package io.wispforest.affinity.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class ConstructSpiritIntegrationApparatusCriterion extends AbstractCriterion<ConstructSpiritIntegrationApparatusCriterion.Conditions> {

    public void trigger(ServerPlayerEntity player, int completion) {
        this.trigger(player, conditions -> conditions.completion.test(completion));
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<LootContextPredicate> player,
                             NumberRange.IntRange completion) implements AbstractCriterion.Conditions {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.optionalField("player", EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, false).forGetter(Conditions::player),
                NumberRange.IntRange.CODEC.fieldOf("completion").forGetter(Conditions::completion)
        ).apply(instance, Conditions::new));
    }
}
