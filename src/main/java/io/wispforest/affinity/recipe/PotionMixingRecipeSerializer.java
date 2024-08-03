package io.wispforest.affinity.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;

import java.util.ArrayList;

public class PotionMixingRecipeSerializer implements RecipeSerializer<PotionMixingRecipe> {

    public PotionMixingRecipeSerializer() {}

    @Override
    public MapCodec<PotionMixingRecipe> codec() {
        return PotionMixingRecipe.CODEC;
    }

    @Override
    public PacketCodec<RegistryByteBuf, PotionMixingRecipe> packetCodec() {
        // TODO: move this to endec.
        return new PacketCodec<RegistryByteBuf, PotionMixingRecipe>() {
            @Override
            public PotionMixingRecipe decode(RegistryByteBuf buf) {
                final var potion = Registries.POTION.get(buf.readVarInt());

                final var effectInputs = buf.readCollection(value -> new ArrayList<>(), $ -> Registries.STATUS_EFFECT.get($.readVarInt()));
                final var itemInputs = buf.readCollection(value -> new ArrayList<>(), $ -> Ingredient.PACKET_CODEC.decode((RegistryByteBuf) $));
                int copyNbtIndex = buf.readVarInt();
                boolean strong = buf.readBoolean();

                return new PotionMixingRecipe(effectInputs, itemInputs, copyNbtIndex, potion, strong);
            }

            @Override
            public void encode(RegistryByteBuf buf, PotionMixingRecipe recipe) {
                buf.writeVarInt(Registries.POTION.getRawId(recipe.potionOutput()));

                buf.writeCollection(recipe.effectInputs, ($, effect) -> $.writeVarInt(Registries.STATUS_EFFECT.getRawId(effect)));
                buf.writeCollection(recipe.itemInputs, ($, ingredient) -> Ingredient.PACKET_CODEC.encode((RegistryByteBuf) $, ingredient));
                buf.writeVarInt(recipe.copyComponentsIndex);
                buf.writeBoolean(recipe.strong);
            }
        };
    }
}
