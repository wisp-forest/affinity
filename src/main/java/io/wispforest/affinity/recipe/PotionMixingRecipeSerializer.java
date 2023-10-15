package io.wispforest.affinity.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;

import java.util.ArrayList;

public class PotionMixingRecipeSerializer implements RecipeSerializer<PotionMixingRecipe> {

    public PotionMixingRecipeSerializer() {}

    @Override
    public Codec<PotionMixingRecipe> codec() {
        return PotionMixingRecipe.CODEC;
    }

    @Override
    public PotionMixingRecipe read(PacketByteBuf buf) {
        final var potion = Registries.POTION.get(buf.readVarInt());

        final var effectInputs = buf.readCollection(value -> new ArrayList<>(), $ -> Registries.STATUS_EFFECT.get($.readVarInt()));
        final var itemInputs = buf.readCollection(value -> new ArrayList<>(), Ingredient::fromPacket);
        int copyNbtIndex = buf.readVarInt();
        boolean strong = buf.readBoolean();

        return new PotionMixingRecipe(effectInputs, itemInputs, copyNbtIndex, potion, strong);
    }

    @Override
    public void write(PacketByteBuf buf, PotionMixingRecipe recipe) {
        buf.writeVarInt(Registries.POTION.getRawId(recipe.potionOutput()));

        buf.writeCollection(recipe.effectInputs, ($, effect) -> $.writeVarInt(Registries.STATUS_EFFECT.getRawId(effect)));
        buf.writeCollection(recipe.itemInputs, ($, ingredient) -> ingredient.write($));
        buf.writeVarInt(recipe.copyNbtIndex);
        buf.writeBoolean(recipe.strong);
    }
}
