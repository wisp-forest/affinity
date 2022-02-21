package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.owo.client.screens.ScreenUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class RitualSocleComposerScreenHandler extends ScreenHandler {

    private final ScreenHandlerContext context;
    private final Inventory inventory;

    public static RitualSocleComposerScreenHandler client(int syncId, PlayerInventory inventory) {
        return new RitualSocleComposerScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public RitualSocleComposerScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(AffinityScreenHandlerTypes.RITUAL_SOCLE_COMPOSER, syncId);
        this.context = context;
        this.inventory = new SimpleInventory(7);

        this.addSlot(new Slot(this.inventory, 0, 80, 37));

        this.addSlot(new Slot(this.inventory, 1, 26, 16));
        this.addSlot(new Slot(this.inventory, 2, 26, 37));
        this.addSlot(new Slot(this.inventory, 3, 26, 58));

        this.addSlot(new Slot(this.inventory, 4, 134, 16));
        this.addSlot(new Slot(this.inventory, 5, 134, 37));
        this.addSlot(new Slot(this.inventory, 6, 134, 58));

        ScreenUtils.generatePlayerSlots(8, 90, inventory, this::addSlot);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, AffinityBlocks.RITUAL_SOCLE_COMPOSER);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return ScreenUtils.handleSlotTransfer(this, index, this.inventory.size());
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.inventory));
    }
}
