package io.wispforest.affinity.recipe;

import io.wispforest.affinity.misc.potion.GlowingPotion;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class GlowingPotionDyeRecipe extends SpecialCraftingRecipe {

    public GlowingPotionDyeRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;

        for (int i = 0; i < input.getSize(); i++) {
            var stack = input.getStackInSlot(i);
            if (stack.getItem() instanceof PotionItem && stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion().orElse(null) instanceof GlowingPotion) {
                if (!isValid(potion)) {
                    potion = stack;
                } else {
                    potion = null;
                    break;
                }
            } else if (stack.getItem() instanceof DyeItem) {
                if (!isValid(dye)) {
                    dye = stack;
                } else {
                    dye = null;
                    break;
                }
            } else if (!stack.isEmpty() && isValid(potion)) {
                potion = null;
                break;
            }
        }

        return isValid(potion) && isValid(dye);
    }

    private boolean isValid(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        var potion = ItemStack.EMPTY;
        var dye = ItemStack.EMPTY;

        for (int i = 0; i < input.getSize(); i++) {
            var stack = input.getStackInSlot(i);
            if (stack.getItem() instanceof PotionItem) {
                potion = stack.copy();
            } else if (stack.getItem() instanceof DyeItem) {
                dye = stack;
            }
        }

        var data = new NbtCompound();
        data.put(GlowingPotion.COLOR_KEY, ((DyeItem) dye.getItem()).getColor());
        potion.put(PotionMixture.EXTRA_DATA, data);

        return potion;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.CRAFTING_SPECIAL_GLOWING_POTION_DYE;
    }
}
