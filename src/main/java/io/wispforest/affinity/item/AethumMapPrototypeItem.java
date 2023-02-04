package io.wispforest.affinity.item;

import io.wispforest.affinity.mixin.access.FilledMapItemInvoker;
import io.wispforest.affinity.mixin.access.MapStateAccessor;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class AethumMapPrototypeItem extends Item {

    public AethumMapPrototypeItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            ItemOps.decrementPlayerHandItem(user, hand);

            var mapStack = AffinityItems.REALIZED_AETHUM_MAP.getDefaultStack();
            FilledMapItemInvoker.affinity$createMapState(mapStack, world, user.getBlockX(), user.getBlockZ(), 0,
                    true, false, world.getRegistryKey());
            var stateAccess = (MapStateAccessor) FilledMapItem.getOrCreateMapState(mapStack, world);
            stateAccess.affinity$setCenterX(RealizedAethumMapItem.makeCenter((int) user.getX()));
            stateAccess.affinity$setCenterZ(RealizedAethumMapItem.makeCenter((int) user.getZ()));

            user.getInventory().offerOrDrop(mapStack);
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

}
