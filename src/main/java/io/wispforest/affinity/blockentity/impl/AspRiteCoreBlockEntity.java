package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.misc.NbtKey;
import io.wispforest.affinity.misc.recipe.AspenInfusionRecipe;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AspRiteCoreBlockEntity extends RitualCoreBlockEntity {

    private final NbtKey<ItemStack> ITEM_KEY = new NbtKey<>("item", NbtKey.Type.ITEM_STACK);
    @NotNull private ItemStack item = ItemStack.EMPTY;

    @Nullable
    private AspenInfusionRecipe cachedRecipe = null;

    public AspRiteCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASP_RITE_CORE, pos, state);
    }

    @Override
    protected ActionResult handleNormalUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(hand).isOf(AffinityItems.WAND_OF_INQUIRY)) return ActionResult.PASS;
        if (this.world.isClient()) return ActionResult.SUCCESS;

        return InteractionUtil.handleSingleItemContainer(this.world, this.pos, player, hand,
                () -> this.item, stack -> this.item = stack, this::markDirty);
    }

    @Override
    protected boolean onRitualStart(RitualConfiguration configuration) {
        if (this.item.isEmpty()) return false;

        final var inventory = new AspenInfusionInventory(configuration.resolveSocles(this.world), this.item);
        final var recipeOptional = this.world.getRecipeManager()
                .getFirstMatch(AffinityRecipeTypes.ASPEN_INFUSION, inventory, this.world);

        if (recipeOptional.isEmpty()) return false;
        this.cachedRecipe = recipeOptional.get();

        return true;
    }

    @Override
    protected boolean onRitualCompleted() {
        this.item = this.cachedRecipe.getOutput();
        return true;
    }

    @Override
    public void onBroken() {
        super.onBroken();
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.getItem());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.item = ITEM_KEY.get(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        ITEM_KEY.put(nbt, this.item);
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    @Override
    protected void doRitualTick() {}

    public static class AspenInfusionInventory extends SocleInventory {

        private final ItemStack primaryInput;

        public AspenInfusionInventory(List<RitualSocleBlockEntity> socles, ItemStack primaryInput) {
            super(socles);
            this.primaryInput = primaryInput.copy();
        }

        public ItemStack primaryInput() {
            return primaryInput;
        }
    }
}
