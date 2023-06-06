package io.wispforest.affinity.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CarbonCopyItem extends Item {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static final NbtKey<NbtList> INPUT_MATRIX_KEY = new NbtKey.ListKey<>("InputMatrix", NbtKey.Type.STRING);
    public static final NbtKey<ItemStack> RESULT_KEY = new NbtKey<>("Result", NbtKey.Type.ITEM_STACK);

    public CarbonCopyItem() {
        super(new OwoItemSettings().maxCount(1));
    }

    @Override
    public Optional<net.minecraft.client.item.TooltipData> getTooltipData(ItemStack stack) {
        var matrix = getInputMatrix(stack);
        var result = stack.getOr(RESULT_KEY, null);
        if (matrix == null || result == null) return Optional.empty();

        return Optional.of(new TooltipData(matrix, result));
    }

    @Override
    public Text getName() {
        return Text.translatable(this.getTranslationKey() + ".empty");
    }

    @Override
    public Text getName(ItemStack stack) {
        var resultStack = stack.getOr(RESULT_KEY, null);
        return resultStack == null
                ? this.getName()
                : Text.translatable(this.getTranslationKey(), resultStack.getCount(), resultStack.getName());
    }

    public static ItemStack create(DefaultedList<Ingredient> inputMatrix, ItemStack result) {
        var stack = AffinityItems.CARBON_COPY.getDefaultStack();

        var inputMatrixNbt = new NbtList();
        for (var ingredient : inputMatrix) {
            inputMatrixNbt.add(NbtString.of(ingredient.toJson().toString()));
        }

        stack.put(INPUT_MATRIX_KEY, inputMatrixNbt);
        stack.put(RESULT_KEY, result);

        return stack;
    }

    public static @Nullable DefaultedList<Ingredient> getInputMatrix(ItemStack copyStack) {
        if (!copyStack.has(INPUT_MATRIX_KEY)) return null;
        var inputMatrixNbt = copyStack.get(INPUT_MATRIX_KEY);
        var inputMatrix = DefaultedList.<Ingredient>ofSize(9);

        for (var inputElement : inputMatrixNbt) {
            if (!(inputElement instanceof NbtString string)) return null;
            inputMatrix.add(Ingredient.fromJson(GSON.fromJson(string.asString(), JsonElement.class)));
        }

        return inputMatrix;
    }

    public record TooltipData(DefaultedList<Ingredient> inputMatrix, ItemStack result) implements net.minecraft.client.item.TooltipData {}

}
