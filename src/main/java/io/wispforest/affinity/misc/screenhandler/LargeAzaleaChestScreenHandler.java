package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.mixin.access.SlotAccessor;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;

public class LargeAzaleaChestScreenHandler extends GenericContainerScreenHandler {

    public static LargeAzaleaChestScreenHandler client(int syncId, PlayerInventory playerInventory) {
        return new LargeAzaleaChestScreenHandler(syncId, playerInventory, new SimpleInventory(90));
    }

    public LargeAzaleaChestScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(AffinityScreenHandlerTypes.LARGE_AZALEA_CHEST, syncId, playerInventory, inventory, 10);

        for (int row = 0; row < 9; row++) {
            for (int column = 0; column < 10; column++) {
                var slot = (SlotAccessor) this.getSlot(row * 10 + column);
                slot.affinity$setX(8 + column * 18);
                slot.affinity$setY(18 + row * 18);
            }
        }

        int playerInventoryBaseIdx = 9 * 10;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                var slot = (SlotAccessor) this.getSlot(playerInventoryBaseIdx + row * 9 + column);
                slot.affinity$setX(17 + column * 18);
                slot.affinity$setY(194 + row * 18);
            }
        }

        int hotbarBaseIdx = playerInventoryBaseIdx + 9 * 3;
        for (int column = 0; column < 9; column++) {
            var slot = (SlotAccessor) this.getSlot(hotbarBaseIdx + column);
            slot.affinity$setX(17 + column * 18);
            slot.affinity$setY(252);
        }
    }
}
