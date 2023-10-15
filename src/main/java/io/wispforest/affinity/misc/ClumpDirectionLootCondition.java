package io.wispforest.affinity.misc;

import com.mojang.serialization.Codec;
import io.wispforest.affinity.block.impl.PeculiarClumpBlock;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.BlockPos;

public class ClumpDirectionLootCondition implements LootCondition {

    public static final LootConditionType TYPE = new LootConditionType(Codec.unit(new ClumpDirectionLootCondition()));

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return PeculiarClumpBlock.getAndClearMinedState(lootContext.getWorld().getServer(), BlockPos.ofFloored(lootContext.requireParameter(LootContextParameters.ORIGIN)));
    }
}
