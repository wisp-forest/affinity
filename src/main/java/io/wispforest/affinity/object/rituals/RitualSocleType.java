package io.wispforest.affinity.object.rituals;

import io.wispforest.affinity.block.impl.RitualSocleBlock;
import io.wispforest.affinity.item.SocleOrnamentItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface RitualSocleType {

    /**
     * @return The color the glow overlay on the
     * socle's model should have
     */
    int glowColor();

    /**
     * @return The amount by which a socle of this type
     * boosts the stability of a ritual, in % of (100 - stability)
     */
    double stabilityModifier();

    /**
     * @return The item used to craft socles of this type,
     * also used when deconstructing a socle of this type
     */
    Item ornamentItem();

    /**
     * @return The actual socle block that this type represents
     */
    Block socleBlock();

    @Nullable
    static RitualSocleType forBlockState(BlockState state) {
        if (!(state.getBlock() instanceof RitualSocleBlock socleBlock)) return null;
        return socleBlock.type();
    }

    @Nullable
    static RitualSocleType forBlockItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return null;
        if (!(blockItem.getBlock() instanceof RitualSocleBlock socleBlock)) return null;
        return socleBlock.type();
    }

    @Nullable
    static RitualSocleType forItem(ItemStack stack) {
        if (!(stack.getItem() instanceof SocleOrnamentItem ornamentItem)) return null;
        return ornamentItem.socleType();
    }

}
