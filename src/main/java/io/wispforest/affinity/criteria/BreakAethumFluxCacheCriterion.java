package io.wispforest.affinity.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.affinity.blockentity.impl.AethumFluxCacheBlockEntity;
import io.wispforest.affinity.object.AffinityCriteria;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class BreakAethumFluxCacheCriterion extends AbstractCriterion<BreakAethumFluxCacheCriterion.Conditions> {

    public void trigger(ServerPlayerEntity player, AethumFluxCacheBlockEntity cache) {
        this.trigger(player, conditions -> conditions.aethum.test((int) cache.flux()) && conditions.aethumPercentage.test(cache.flux() / (double) cache.fluxCapacity()));
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<LootContextPredicate> player,
                             NumberRange.IntRange aethum,
                             NumberRange.DoubleRange aethumPercentage) implements AbstractCriterion.Conditions {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.optionalField("player", EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, false).forGetter(Conditions::player),
                Codec.optionalField("aethum", NumberRange.IntRange.CODEC, false).xmap($ -> $.orElse(NumberRange.IntRange.ANY), Optional::of).forGetter(Conditions::aethum),
                Codec.optionalField("aethum_percentage", NumberRange.DoubleRange.CODEC, false).xmap($ -> $.orElse(NumberRange.DoubleRange.ANY), Optional::of).forGetter(Conditions::aethumPercentage)
        ).apply(instance, Conditions::new));
    }

    static {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(blockEntity instanceof AethumFluxCacheBlockEntity cache)) return true;

            AffinityCriteria.BREAK_AETHUM_FLUX_CACHE.trigger((ServerPlayerEntity) player, cache);
            return true;
        });
    }

}
