package io.wispforest.affinity.util.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

public class PotionMixingRecipeSerializer implements RecipeSerializer<PotionMixingRecipe> {

    private PotionMixingRecipeSerializer() {}

    public static final PotionMixingRecipeSerializer INSTANCE = new PotionMixingRecipeSerializer();
    public static final Identifier ID = PotionMixingRecipe.Type.ID;

    @Override
    public PotionMixingRecipe read(Identifier id, JsonObject json) {

        final var effectInputsJson = JsonHelper.getArray(json, "effect_inputs");
        final var itemInputsJson = JsonHelper.getArray(json, "item_inputs");

        final var outputPotion = Registry.POTION.getOrEmpty(Identifier.tryParse(JsonHelper.getString(json, "output"))).orElseThrow(() -> new JsonSyntaxException("Invalid potion: " + JsonHelper.getString(json, "output")));

        final var inputEffects = new ArrayList<StatusEffect>();
        for (var element : effectInputsJson) {
            inputEffects.add(Registry.STATUS_EFFECT.getOrEmpty(Identifier.tryParse(element.getAsString())).orElseThrow(() -> new JsonSyntaxException("Invalid status effect: " + element.getAsString())));
        }

        final var itemInputs = new ArrayList<Ingredient>();
        for (var element : itemInputsJson) {
            itemInputs.add(Ingredient.fromJson(element));
        }

        return new PotionMixingRecipe(id, itemInputs, inputEffects, outputPotion);
    }

    @Override
    public PotionMixingRecipe read(Identifier id, PacketByteBuf buf) {
        final var potion = Registry.POTION.get(buf.readVarInt());

        final var effectInputs = buf.readCollection(value -> new ArrayList<>(), buf1 -> Registry.STATUS_EFFECT.get(buf1.readVarInt()));
        final var itemInputs = buf.readCollection(value -> new ArrayList<>(), Ingredient::fromPacket);

        return new PotionMixingRecipe(id, itemInputs, effectInputs, potion);
    }

    @Override
    public void write(PacketByteBuf buf, PotionMixingRecipe recipe) {
        buf.writeVarInt(Registry.POTION.getRawId(recipe.getPotionOutput()));

        buf.writeCollection(recipe.getEffectInputs(), (buf1, effect) -> buf1.writeVarInt(Registry.STATUS_EFFECT.getRawId(effect)));
        buf.writeCollection(recipe.getItemInputs(), (buf1, ingredient) -> ingredient.write(buf1));
    }
}