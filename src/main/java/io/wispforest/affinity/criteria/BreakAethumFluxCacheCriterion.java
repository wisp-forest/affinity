package io.wispforest.affinity.criteria;

import com.google.gson.JsonObject;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.AethumFluxCacheBlockEntity;
import io.wispforest.affinity.object.AffinityCriteria;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class BreakAethumFluxCacheCriterion extends AbstractCriterion<BreakAethumFluxCacheCriterion.Conditions> {

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> predicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(predicate, NumberRange.IntRange.fromJson(obj.get("aethum")), NumberRange.DoubleRange.fromJson(obj.get("aethum_percentage")));
    }

    public void trigger(ServerPlayerEntity player, AethumFluxCacheBlockEntity cache) {
        this.trigger(player, conditions -> conditions.aethum.test((int) cache.flux()) && conditions.aethumPercentage.test(cache.flux() / (double) cache.fluxCapacity()));
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final NumberRange.IntRange aethum;
        private final NumberRange.DoubleRange aethumPercentage;

        public Conditions(Optional<LootContextPredicate> player, NumberRange.IntRange aethum, NumberRange.DoubleRange aethumPercentage) {
            super(player);
            this.aethum = aethum;
            this.aethumPercentage = aethumPercentage;
        }
    }

    static {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(blockEntity instanceof AethumFluxCacheBlockEntity cache)) return true;

            AffinityCriteria.BREAK_AETHUM_FLUX_CACHE.trigger((ServerPlayerEntity) player, cache);
            return true;
        });
    }

}
