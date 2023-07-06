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

public class BreakAethumFluxCacheCriterion extends AbstractCriterion<BreakAethumFluxCacheCriterion.Conditions> {

    public static final Identifier ID = Affinity.id("break_aethum_flux_cache");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate, NumberRange.IntRange.fromJson(obj.get("aethum")), NumberRange.FloatRange.fromJson(obj.get("aethum_percentage")));
    }

    public void trigger(ServerPlayerEntity player, AethumFluxCacheBlockEntity cache) {
        this.trigger(player, conditions -> conditions.aethum.test((int) cache.flux()) && conditions.aethumPercentage.test(cache.flux() / (double) cache.fluxCapacity()));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final NumberRange.IntRange aethum;
        private final NumberRange.FloatRange aethumPercentage;

        public Conditions(LootContextPredicate player, NumberRange.IntRange aethum, NumberRange.FloatRange aethumPercentage) {
            super(ID, player);
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
