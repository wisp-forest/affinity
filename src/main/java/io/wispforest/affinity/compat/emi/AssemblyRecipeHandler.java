package io.wispforest.affinity.compat.emi;

import com.google.common.collect.Lists;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import io.wispforest.affinity.misc.screenhandler.AssemblyAugmentScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AssemblyRecipeHandler implements StandardRecipeHandler<AssemblyAugmentScreenHandler> {

    @Override
    public List<Slot> getInputSources(AssemblyAugmentScreenHandler handler) {
        List<Slot> list = Lists.newArrayList();
        for (int i = 1; i < 10; i++) {
            list.add(handler.getSlot(i));
        }
        int invStart = 10;
        for (int i = invStart; i < invStart + 36; i++) {
            list.add(handler.getSlot(i));
        }
        return list;
    }

    @Override
    public List<Slot> getCraftingSlots(AssemblyAugmentScreenHandler handler) {
        List<Slot> list = Lists.newArrayList();
        for (int i = 1; i < 10; i++) {
            list.add(handler.getSlot(i));
        }
        return list;
    }

    @Override
    public @Nullable Slot getOutputSlot(AssemblyAugmentScreenHandler handler) {
        return handler.slots.get(0);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return (recipe.getCategory() == AffinityEmiPlugin.ASSEMBLY || recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING) && recipe.supportsRecipeTree();
    }
}
