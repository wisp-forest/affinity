package io.wispforest.affinity.misc.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    /**
     * Tries to parse an ItemStack from the given JSON,
     * including nbt data
     *
     * @param json The root json object
     * @param key  The key of the ItemStack to parse
     * @return The parse {@link ItemStack}
     * @throws JsonSyntaxException If any of the input data is invalid,
     *                             a respective exception is thrown
     */
    public static ItemStack readChadStack(JsonObject json, String key) {
        final var stackObject = JsonHelper.getObject(json, key);
        final var item = JsonHelper.getItem(stackObject, "item");
        final var count = JsonHelper.getInt(stackObject, "count", 1);

        final var stack = new ItemStack(item, count);

        if (stackObject.has("data")) {
            stack.setNbt(readNbt(json, "data"));
        }

        return stack;
    }

    public static <T> T readFromRegistry(JsonObject json, String key, Registry<T> registry) {
        return registry.get(new Identifier(JsonHelper.getString(json, key)));
    }

    public static NbtCompound readNbt(JsonObject json, String key) {
        final var nbtString = JsonHelper.getObject(json, key).toString();
        final var reader = new StringNbtReader(new StringReader(nbtString));

        try {
            return reader.parseCompound();
        } catch (CommandSyntaxException e) {
            throw new JsonParseException("Invalid NBT data found: '" + nbtString + "'", e);
        }
    }

    public static List<Ingredient> readIngredientList(JsonObject json, String key) {
        final var array = JsonHelper.getArray(json, key);
        final var list = new ArrayList<Ingredient>();

        for (var element : array) {
            list.add(Ingredient.fromJson(element));
        }

        return list;
    }

}
