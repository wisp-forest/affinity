package io.wispforest.affinity.misc.recipe;

import io.wispforest.affinity.misc.potion.GlowingPotion;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class GlowingPotionDyeRecipe extends SpecialCraftingRecipe {

    public static final SpecialRecipeSerializer<GlowingPotionDyeRecipe> SERIALIZER = new SpecialRecipeSerializer<>(GlowingPotionDyeRecipe::new);

    protected GlowingPotionDyeRecipe(Identifier id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;

        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
            if (stack.isOf(Items.POTION) && PotionUtil.getPotion(stack) instanceof GlowingPotion) {
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
            if (stack.isOf(Items.POTION)) {
                potion = stack.copy();
            } else if (stack.getItem() instanceof DyeItem) {
                dye = stack;
            }
        }

        var nbt = potion.getOrCreateNbt();
        nbt.putString("Color", ((DyeItem) dye.getItem()).getColor().asString());

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
