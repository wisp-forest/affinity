package io.wispforest.affinity.recipe;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.affinity.blockentity.impl.SpiritIntegrationApparatusBlockEntity;
import io.wispforest.affinity.misc.util.JsonUtil;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SpiritAssimilationRecipe extends RitualRecipe<SpiritIntegrationApparatusBlockEntity.SpiritAssimilationInventory> {

    private static final Codec<SpiritAssimilationRecipe> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("core_inputs").forGetter(recipe -> recipe.coreInputs),
                    Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("socle_inputs").forGetter(recipe -> recipe.socleInputs),
                    EntityData.CODEC.fieldOf("entity").forGetter(recipe -> new EntityData(recipe.entityType, Optional.ofNullable(recipe.entityNbt))),
                    RecipeCodecs.CRAFTING_RESULT.fieldOf("output").forGetter(recipe -> recipe.output),
                    Codec.INT.optionalFieldOf("duration", 100).forGetter(recipe -> recipe.duration),
                    Codecs.validate(Codec.INT, duration -> {
                        if (duration % 4 != 0) {
                            return DataResult.error(() -> "Spirit assimilation flux cost must be divisible by 4");
                        } else {
                            return DataResult.success(duration);
                        }
                    }).optionalFieldOf("flux_cost_per_tick", 0).forGetter(recipe -> recipe.fluxCostPerTick)
            )
            .apply(instance, (coreInputs, socleInputs, entityData, result, duration, fluxCost) -> {
                return new SpiritAssimilationRecipe(coreInputs, socleInputs, entityData.type, entityData.nbt.orElse(null), duration, fluxCost, result);
            }));

    public final List<Ingredient> coreInputs;
    public final EntityType<?> entityType;
    @Nullable private final NbtCompound entityNbt;

    private final ItemStack output;

    protected SpiritAssimilationRecipe(List<Ingredient> coreInputs,
                                       List<Ingredient> inputs,
                                       EntityType<?> entityType,
                                       @Nullable NbtCompound entityNbt,
                                       int duration,
                                       int fluxCostPerTick,
                                       ItemStack output) {
        super(inputs, duration, fluxCostPerTick);
        this.coreInputs = ImmutableList.copyOf(coreInputs);
        this.entityType = entityType;
        this.entityNbt = entityNbt;
        this.output = output;
    }

    @Override
    public boolean matches(SpiritIntegrationApparatusBlockEntity.SpiritAssimilationInventory inventory, World world) {
        return this.doShapelessMatch(this.coreInputs, Arrays.asList(inventory.coreInputs()))
                && this.doShapelessMatch(this.socleInputs, inventory.delegate())
                && this.entityType == inventory.sacrifice().getType()
                && (this.entityNbt == null || NbtHelper.matches(this.entityNbt, inventory.sacrifice().writeNbt(new NbtCompound()), true));
    }

    @Override
    public ItemStack craft(SpiritIntegrationApparatusBlockEntity.SpiritAssimilationInventory inventory, DynamicRegistryManager drm) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager drm) {
        return this.output.copy();
    }

    public @Nullable NbtCompound entityNbt() {
        return this.entityNbt != null
                ? this.entityNbt.copy()
                : null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.SPIRIT_ASSIMILATION;
    }

    @Override
    public RecipeType<?> getType() {
        return AffinityRecipeTypes.SPIRIT_ASSIMILATION;
    }

    private record EntityData(EntityType<?> type, Optional<NbtCompound> nbt) {
        public static final Codec<EntityData> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        Registries.ENTITY_TYPE.getCodec().fieldOf("id").forGetter(EntityData::type),
                        NbtCompound.CODEC.optionalFieldOf("data").forGetter(EntityData::nbt)
                ).apply(instance, EntityData::new));
    }

    public static final class Serializer implements RecipeSerializer<SpiritAssimilationRecipe> {

        public Serializer() {}

        @Override
        public Codec<SpiritAssimilationRecipe> codec() {
            return SpiritAssimilationRecipe.CODEC;
        }

        @Override
        public SpiritAssimilationRecipe read(PacketByteBuf buf) {
            return new SpiritAssimilationRecipe(
                    buf.readCollection(ArrayList::new, Ingredient::fromPacket),
                    buf.readCollection(ArrayList::new, Ingredient::fromPacket),
                    Registries.ENTITY_TYPE.get(buf.readVarInt()),
                    buf.readOptional(PacketByteBuf::readNbt).orElse(null),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readItemStack()
            );
        }

        @Override
        public void write(PacketByteBuf buf, SpiritAssimilationRecipe recipe) {
            buf.writeCollection(recipe.coreInputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));
            buf.writeCollection(recipe.socleInputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));

            buf.writeVarInt(Registries.ENTITY_TYPE.getRawId(recipe.entityType));
            buf.writeOptional(Optional.ofNullable(recipe.entityNbt), PacketByteBuf::writeNbt);

            buf.writeVarInt(recipe.duration);
            buf.writeVarInt(recipe.fluxCostPerTick);

            buf.writeItemStack(recipe.output);
        }
    }
}
