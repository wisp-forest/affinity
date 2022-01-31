package io.wispforest.affinity.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.wispforest.affinity.block.impl.PeculiarClumpBlock;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.math.BlockPos;

public class ClumpDirectionLootCondition implements LootCondition {

    public static final LootConditionType TYPE = new LootConditionType(new Serializer());

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return PeculiarClumpBlock.getAndClearMinedState(new BlockPos(lootContext.requireParameter(LootContextParameters.ORIGIN)));
    }

    private static class Serializer implements JsonSerializer<ClumpDirectionLootCondition> {

        @Override
        public void toJson(JsonObject json, ClumpDirectionLootCondition object, JsonSerializationContext context) {}

        @Override
        public ClumpDirectionLootCondition fromJson(JsonObject json, JsonDeserializationContext context) {
            return new ClumpDirectionLootCondition();
        }
    }
}
