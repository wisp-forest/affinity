package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AzaleaBowItem extends BowItem {

    public static final float AETHUM_COST_PER_SHOT = 1f;

    public AzaleaBowItem() {
        super(AffinityItems.settings().maxCount(1).maxDamage(500));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.affinity.azalea_bow.tooltip.consumption_per_shot", MathUtil.rounded(AETHUM_COST_PER_SHOT, 2)));
    }
}
