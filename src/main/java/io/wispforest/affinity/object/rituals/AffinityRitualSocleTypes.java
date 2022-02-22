package io.wispforest.affinity.object.rituals;

import io.wispforest.affinity.block.impl.RitualSocleBlock;
import io.wispforest.affinity.item.SocleOrnamentItem;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public enum AffinityRitualSocleTypes implements RitualSocleType {

    RUDIMENTARY(0x94B3FD, 0, () -> AffinityItems.STONE_SOCLE_ORNAMENT, () -> AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE),
    REFINED(0x2FDD92, .5, () -> AffinityItems.PRISMARINE_SOCLE_ORNAMENT, () -> AffinityBlocks.REFINED_RITUAL_SOCLE),
    SOPHISTICATED(0xCE7BB0, .15, () -> AffinityItems.PURPUR_SOCLE_ORNAMENT, () -> AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE);

    private final int glowColor;
    private final double stabilityModifier;
    private final Supplier<Item> ornamentItem;
    private final Supplier<Block> socleBlock;

    AffinityRitualSocleTypes(int glowColor, double stabilityModifier, Supplier<Item> ornamentItem, Supplier<Block> socleBlock) {
        this.glowColor = glowColor;
        this.stabilityModifier = stabilityModifier;
        this.ornamentItem = ornamentItem;
        this.socleBlock = socleBlock;
    }

    @Nullable
    public static RitualSocleType forBlockItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return null;
        if (!(blockItem.getBlock() instanceof RitualSocleBlock socleBlock)) return null;
        return socleBlock.type();
    }

    @Nullable
    public static RitualSocleType forItem(ItemStack stack) {
        if (!(stack.getItem() instanceof SocleOrnamentItem ornamentItem)) return null;
        return ornamentItem.socleType();
    }

    @Override
    public int glowColor() {
        return this.glowColor;
    }

    @Override
    public double stabilityModifier() {
        return this.stabilityModifier;
    }

    @Override
    public Item ornamentItem() {
        return this.ornamentItem.get();
    }

    @Override
    public Block socleBlock() {
        return this.socleBlock.get();
    }
}
