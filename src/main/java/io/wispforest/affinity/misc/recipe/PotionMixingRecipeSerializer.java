package io.wispforest.affinity.misc.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.wispforest.affinity.misc.Ingrediente;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;

public class PotionMixingRecipeSerializer implements RecipeSerializer<PotionMixingRecipe> {

    public static final PotionMixingRecipeSerializer INSTANCE = new PotionMixingRecipeSerializer();

    private static final Ingrediente.Serializer<Boolean> INGREDIENTE_SERIALIZER = Ingrediente.makeSerializer(
            PacketByteBuf::writeBoolean,
            PacketByteBuf::readBoolean,
            object -> JsonHelper.getBoolean(object, "copy_nbt", false)
    );

    private PotionMixingRecipeSerializer() {}

    @Override
    public PotionMixingRecipe read(Identifier id, JsonObject json) {

        final var effectInputsJson = JsonHelper.getArray(json, "effect_inputs");
        final var itemInputsJson = JsonHelper.getArray(json, "item_inputs");

        final var outputPotion = Registries.POTION.getOrEmpty(Identifier.tryParse(JsonHelper.getString(json, "output"))).orElseThrow(() -> new JsonSyntaxException("Invalid potion: " + JsonHelper.getString(json, "output")));

        final var inputEffects = new ArrayList<StatusEffect>();
        for (var element : effectInputsJson) {
            inputEffects.add(Registries.STATUS_EFFECT.getOrEmpty(Identifier.tryParse(element.getAsString())).orElseThrow(() -> new JsonSyntaxException("Invalid status effect: " + element.getAsString())));
        }

        final var itemInputs = new ArrayList<Ingrediente<Boolean>>();
        for (var element : itemInputsJson) {
            itemInputs.add(INGREDIENTE_SERIALIZER.fromJson(element));
        }

        return new PotionMixingRecipe(id, itemInputs, inputEffects, outputPotion);
    }

    @Override
    public PotionMixingRecipe read(Identifier id, PacketByteBuf buf) {
        final var potion = Registries.POTION.get(buf.readVarInt());

        final var effectInputs = buf.readCollection(value -> new ArrayList<>(), buf1 -> Registries.STATUS_EFFECT.get(buf1.readVarInt()));
        final var itemInputs = buf.readCollection(value -> new ArrayList<>(), INGREDIENTE_SERIALIZER::fromPacket);

        return new PotionMixingRecipe(id, itemInputs, effectInputs, potion);
    }

    @Override
    public void write(PacketByteBuf buf, PotionMixingRecipe recipe) {
        buf.writeVarInt(Registries.POTION.getRawId(recipe.potionOutput()));

        buf.writeCollection(recipe.getEffectInputs(), (buf1, effect) -> buf1.writeVarInt(Registries.STATUS_EFFECT.getRawId(effect)));
        buf.writeCollection(recipe.getItemInputs(), INGREDIENTE_SERIALIZER::writeToPacket);
    }
}
