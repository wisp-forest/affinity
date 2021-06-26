package com.glisco.nidween.block;

import com.glisco.nidween.registries.NidweenBlocks;
import com.glisco.nidween.util.potion.PotionMixture;
import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;

public class BrewingCauldronBlockEntity extends BlockEntity implements BlockEntityClientSerializable {

    @NotNull
    private PotionMixture currentPotion = PotionMixture.EMPTY;
    private int fillLevel = 0;

    public BrewingCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(NidweenBlocks.BlockEntityTypes.BREWING_CAULDRON, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        currentPotion = PotionMixture.fromNbt(nbt.getCompound("PotionMixture"));
        fillLevel = nbt.getInt("FillLevel");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("PotionMixture", currentPotion.toNbt());
        nbt.putInt("FillLevel", fillLevel);

        return super.writeNbt(nbt);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!world.isClient()) {
            this.sync();
        }
    }

    public float getFluidHeight() {
        return 0.3f + fillLevel * 0.2f;
    }

    public boolean canPotionBeExtracted() {
        return fillLevel > 0;
    }

    public ItemStack extractOneBottle() {
        if (!canPotionBeExtracted()) return ItemStack.EMPTY;

        final var currentPotionBackup = this.currentPotion;

        this.fillLevel--;
        if (fillLevel == 0) this.currentPotion = PotionMixture.EMPTY;
        this.markDirty();

        return currentPotionBackup.toStack();
    }

    public boolean canPotionBeAdded(PotionMixture potion) {
        if (fillLevel > 2) return false;
        return currentPotion.isEmpty() || potion.equals(currentPotion);
    }

    public void addOneBottle(PotionMixture potion) {
        if (!canPotionBeAdded(potion)) return;

        if (fillLevel == 0) {
            this.currentPotion = potion;
        }

        this.fillLevel++;
        this.markDirty();
    }

    public void setCurrentPotion(@NotNull PotionMixture currentPotion) {
        this.currentPotion = currentPotion;
        this.markDirty();
    }

    public @NotNull PotionMixture getCurrentPotion() {
        return currentPotion;
    }
}
