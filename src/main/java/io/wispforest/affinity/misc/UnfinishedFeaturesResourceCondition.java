package io.wispforest.affinity.misc;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.Affinity;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class UnfinishedFeaturesResourceCondition implements ResourceCondition {

    public static final TagKey<Item> UNFINISHED_ITEMS = TagKey.of(RegistryKeys.ITEM, Affinity.id("unfinished"));

    public static final ResourceConditionType<?> TYPE = new ResourceConditionType<>() {
        @Override
        public Identifier id() {
            return Affinity.id("unfinished_features");
        }

        @Override
        public MapCodec<ResourceCondition> codec() {
            return MapCodec.unit(UnfinishedFeaturesResourceCondition::new);
        }
    };

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(RegistryWrapper.@Nullable WrapperLookup registryLookup) {
        return Affinity.config().unfinishedFeatures();
    }
}
