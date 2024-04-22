package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.mixin.access.CraftingInventoryAccessor;
import io.wispforest.affinity.mixin.access.CraftingScreenHandlerAccessor;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.client.screens.SyncedProperty;
import io.wispforest.owo.client.screens.ValidatingSlot;
import io.wispforest.owo.util.Observable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AssemblyAugmentScreenHandler extends CraftingScreenHandler {

    private final @Nullable AssemblyAugmentBlockEntity augment;
    private final SyncedProperty<Integer> displayTreetaps;
    private final SyncedProperty<Float> craftingProgress;

    private final Observable<Identifier> autocraftingRecipeId = Observable.of(null);
    private CraftingRecipe autocraftingRecipe;

    public static AssemblyAugmentScreenHandler client(int syncId, PlayerInventory inventory) {
        MixinHooks.injectAssemblyAugmentScreen = true;
        return new AssemblyAugmentScreenHandler(syncId, inventory, null);
    }

    public static AssemblyAugmentScreenHandler server(int syncId, PlayerInventory inventory, AssemblyAugmentBlockEntity augment) {
        MixinHooks.injectAssemblyAugmentScreen = true;
        return new AssemblyAugmentScreenHandler(syncId, inventory, augment);
    }

    private AssemblyAugmentScreenHandler(int syncId, PlayerInventory inventory, @Nullable AssemblyAugmentBlockEntity augment) {
        super(syncId, inventory, augment == null ? ScreenHandlerContext.EMPTY : ScreenHandlerContext.create(augment.getWorld(), augment.getPos()));
        this.augment = augment;

        this.displayTreetaps = this.createProperty(int.class, 0);
        this.craftingProgress = this.createProperty(float.class, 0f);

        this.addClientboundMessage(SetAutocraftingRecipeMessage.class, message -> {
            if (message.recipeId.isPresent()) {
                var recipe = this.player().getWorld().getRecipeManager().get(message.recipeId.get()).orElse(null);
                this.autocraftingRecipe = recipe instanceof CraftingRecipe craftingRecipe ? craftingRecipe : null;
            } else {
                this.autocraftingRecipe = null;
            }
        });

        this.addSlot(new ValidatingSlot(this.augment != null ? this.augment.templateInventory() : new SimpleInventory(1), 0, 8, 35, stack -> stack.isOf(AffinityItems.CARBON_COPY)));
        this.addSlot(new ValidatingSlot(this.augment != null ? this.augment.inventory() : new SimpleInventory(10), AssemblyAugmentBlockEntity.OUTPUT_SLOT, this.getSlot(0).x, this.getSlot(0).y, stack -> false));

        if (this.augment != null) {
            ((CraftingInventoryAccessor) ((CraftingScreenHandlerAccessor) this).affinity$getInput()).affinity$setStacks(
                    this.augment.inventory().stacks
            );

            this.autocraftingRecipeId.observe(identifier -> {
                this.sendMessage(new SetAutocraftingRecipeMessage(Optional.ofNullable(identifier)));
            });
        }

        this.onContentChanged(((CraftingScreenHandlerAccessor) this).affinity$getInput());
    }

    @Override
    protected void dropInventory(PlayerEntity player, Inventory inventory) {}

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (this.augment != null) this.augment.markDirty();
    }

    @Override
    public void sendContentUpdates() {
        if (this.augment != null) {
            this.onContentChanged(((CraftingScreenHandlerAccessor) this).affinity$getInput());

            this.craftingProgress.set(this.augment.craftingTick() / (float) this.augment.craftingDuration());
            this.displayTreetaps.set(this.augment.displayTreetaps());

            if (this.augment.autocraftingRecipe() != null) {
                this.autocraftingRecipeId.set(this.augment.autocraftingRecipe().getId());
            } else {
                this.autocraftingRecipeId.set(null);
            }

            this.augment.markDirty();
        }

        super.sendContentUpdates();
    }

    public int treetapCount() {
        return this.displayTreetaps.get();
    }

    public float craftingProgress() {
        return this.craftingProgress.get();
    }

    public boolean matchesAutocraftingRecipe() {
        return this.autocraftingRecipe != null && this.autocraftingRecipe.matches(((CraftingScreenHandlerAccessor) this).affinity$getInput(), this.player().getWorld());
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (this.augment == null) return false;
        return this.augment.getWorld().getBlockState(this.augment.getPos()).isOf(AffinityBlocks.ASSEMBLY_AUGMENT);
    }

    public record SetAutocraftingRecipeMessage(Optional<Identifier> recipeId) {}
}
