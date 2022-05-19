package io.wispforest.affinity.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wispforest.affinity.misc.util.JsonUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public record Ingrediente<T>(Ingredient itemPredicate, int minCount, @Nullable NbtCompound nbtModel, T extraData) implements Predicate<ItemStack> {

    @Override
    public boolean test(ItemStack stack) {
        if (!this.itemPredicate.test(stack)) return false;
        if (stack.getCount() < this.minCount) return false;

        return this.nbtModel == null || (stack.hasNbt() && NbtHelper.matches(this.nbtModel, stack.getNbt(), false));
    }

    public static <T> Serializer<T> makeSerializer(BiConsumer<PacketByteBuf, T> packetWriter, Function<PacketByteBuf, T> packetReader, Function<JsonObject, T> jsonReader) {
        return new Serializer<>() {
            @Override
            public void writeToPacket(PacketByteBuf buf, Ingrediente<T> ingrediente) {
                ingrediente.itemPredicate.write(buf);
                buf.writeVarInt(ingrediente.minCount);

                buf.writeBoolean(ingrediente.nbtModel != null);
                if (ingrediente.nbtModel != null) {
                    buf.writeNbt(ingrediente.nbtModel);
                }

                packetWriter.accept(buf, ingrediente.extraData);
            }

            @Override
            public Ingrediente<T> fromPacket(PacketByteBuf buf) {
                final var itemPredicate = Ingredient.fromPacket(buf);
                final int minCount = buf.readVarInt();

                NbtCompound nbtModel = null;
                if (buf.readBoolean()) {
                    nbtModel = buf.readNbt();
                }

                final T extraData = packetReader.apply(buf);

                return new Ingrediente<>(itemPredicate, minCount, nbtModel, extraData);
            }

            @Override
            public Ingrediente<T> fromJson(JsonElement element) {
                final var itemPredicate = Ingredient.fromJson(element);

                int minCount = 1;
                NbtCompound nbtModel = null;
                T extraData = null;

                if (element.isJsonObject()) {
                    final var object = element.getAsJsonObject();

                    minCount = JsonHelper.getInt(object, "count", 1);

                    if (object.has("data")) {
                        nbtModel = JsonUtil.readNbt(object, "data");
                    }

                    extraData = jsonReader.apply(object);
                }

                return new Ingrediente<>(itemPredicate, minCount, nbtModel, extraData);
            }
        };
    }

    public interface Serializer<T> {
        void writeToPacket(PacketByteBuf buf, Ingrediente<T> ingrediente);

        Ingrediente<T> fromPacket(PacketByteBuf buf);

        Ingrediente<T> fromJson(JsonElement element);
    }
}
