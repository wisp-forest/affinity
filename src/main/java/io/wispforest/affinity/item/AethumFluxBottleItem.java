package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

@SuppressWarnings("UnstableApiUsage")
public class AethumFluxBottleItem extends Item {

    public AethumFluxBottleItem() {
        super(new Settings().group(Affinity.AFFINITY_GROUP).maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var member = Affinity.AETHUM_MEMBER.find(context.getWorld(), context.getBlockPos(), null);
        if (member == null) return ActionResult.PASS;
        if (context.getWorld().isClient) return ActionResult.SUCCESS;

        try (var transaction = Transaction.openOuter()) {
            member.insert(1000, transaction);
            transaction.commit();
        }

        return ActionResult.SUCCESS;
    }
}