package io.wispforest.affinity.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.affinity.blockentity.impl.AspRiteCoreBlockEntity;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AspenInfusionRecipe extends RitualRecipe<AspRiteCoreBlockEntity.AspenInfusionInventory> {

    private static final Codec<AspenInfusionRecipe> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("primary_input").forGetter(recipe -> recipe.primaryInput),
                    Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("inputs").forGetter(recipe -> recipe.socleInputs),
                    RecipeCodecs.CRAFTING_RESULT.fieldOf("output").forGetter(recipe -> recipe.output),
                    Codec.INT.optionalFieldOf("duration", 100).forGetter(recipe -> recipe.duration),
                    Codec.INT.optionalFieldOf("flux_cost_per_tick", 0).forGetter(recipe -> recipe.fluxCostPerTick)
            )
            .apply(instance, AspenInfusionRecipe::new));

    public final Ingredient primaryInput;
    private final ItemStack output;

    public AspenInfusionRecipe(Ingredient primaryInput, List<Ingredient> inputs, ItemStack output, int duration, int fluxCostPerTick) {
        super(inputs, duration, fluxCostPerTick);
        this.primaryInput = primaryInput;
        this.output = output;
    }

    @Override
    public boolean matches(AspRiteCoreBlockEntity.AspenInfusionInventory inventory, World world) {
        return this.primaryInput.test(inventory.primaryInput()) && this.doShapelessMatch(this.socleInputs, inventory.delegate());
    }

    @Override
    public ItemStack craft(AspRiteCoreBlockEntity.AspenInfusionInventory inventory, DynamicRegistryManager drm) {
        return this.output.copy();
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager drm) {
        return this.output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.ASPEN_INFUSION;
    }

    @Override
    public RecipeType<?> getType() {
        return AffinityRecipeTypes.ASPEN_INFUSION;
    }

    public static final class Serializer implements RecipeSerializer<AspenInfusionRecipe> {

        @Override
        public Codec<AspenInfusionRecipe> codec() {
            return AspenInfusionRecipe.CODEC;
        }

        @Override
        public AspenInfusionRecipe read(PacketByteBuf buf) {
            return new AspenInfusionRecipe(
                    Ingredient.fromPacket(buf),
                    buf.readCollection(ArrayList::new, Ingredient::fromPacket),
                    buf.readItemStack(),
                    buf.readVarInt(),
                    buf.readVarInt()
            );
        }

        @Override
        public void write(PacketByteBuf buf, AspenInfusionRecipe recipe) {
            recipe.primaryInput.write(buf);
            buf.writeCollection(recipe.socleInputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));
            buf.writeItemStack(recipe.output);
            buf.writeVarInt(recipe.duration);
            buf.writeVarInt(recipe.fluxCostPerTick);
        }
    }
}
