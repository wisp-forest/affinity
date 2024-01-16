package io.wispforest.affinity.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class MinedPeculiarClumpCriterion extends AbstractCriterion<MinedPeculiarClumpCriterion.Conditions> {

    public void trigger(ServerPlayerEntity player, boolean wasMinedCorrectly) {
        this.trigger(player, conditions -> conditions.expectSuccess == wasMinedCorrectly);
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<LootContextPredicate> player,
                             boolean expectSuccess) implements AbstractCriterion.Conditions {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player").forGetter(Conditions::player),
                Codec.BOOL.fieldOf("expect_success").forGetter(Conditions::expectSuccess)
        ).apply(instance, Conditions::new));
    }
}
