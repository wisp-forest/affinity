package io.wispforest.affinity.recipe;

import io.wispforest.affinity.misc.potion.GlowingPotion;
import io.wispforest.affinity.misc.potion.PotionMixture;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class GlowingPotionDyeRecipe extends SpecialCraftingRecipe {

    public static final SpecialRecipeSerializer<GlowingPotionDyeRecipe> SERIALIZER = new SpecialRecipeSerializer<>(GlowingPotionDyeRecipe::new);

    protected GlowingPotionDyeRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;

        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
            if (stack.getItem() instanceof PotionItem && PotionUtil.getPotion(stack) instanceof GlowingPotion) {
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
    public ItemStack craft(CraftingInventory inventory) {
        var potion = ItemStack.EMPTY;
        var dye = ItemStack.EMPTY;

        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
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
        return SERIALIZER;
    }
}
