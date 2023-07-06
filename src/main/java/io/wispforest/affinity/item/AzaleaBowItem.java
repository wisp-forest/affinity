package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AzaleaBowItem extends BowItem {

    public static final float AETHUM_COST_PER_SHOT = 1f;

    public AzaleaBowItem() {
        super(AffinityItems.settings(AffinityItemGroup.EQUIPMENT).maxCount(1).maxDamage(500));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.affinity.azalea_bow.tooltip.consumption_per_shot", MathUtil.rounded(AETHUM_COST_PER_SHOT, 2)));
    }
}
