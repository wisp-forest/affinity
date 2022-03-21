package io.wispforest.affinity.misc.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.JsonHelper;

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

        final var nbtJson = JsonHelper.getObject(stackObject, "data", null);
        if (nbtJson != null) {
            final var nbtString = nbtJson.getAsString();
            final var reader = new StringNbtReader(new StringReader(nbtString));

            try {
                stack.setNbt(reader.parseCompound());
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid NBT data found for item '" + item + "' with json '" + stackObject.getAsString() + "'", e);
            }
        }

        return stack;
    }

}
