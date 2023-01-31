package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.ValidatingSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

public class OuijaBoardScreenHandler extends ScreenHandler {

    private final SimpleInventory inventory = new SimpleInventory(1);
    private final ScreenHandlerContext context;

    public static OuijaBoardScreenHandler client(int syncId, PlayerInventory playerInventory) {
        return new OuijaBoardScreenHandler(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public OuijaBoardScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(AffinityScreenHandlerTypes.OUIJA_BOARD, syncId);
        this.context = context;

        this.addSlot(new ValidatingSlot(this.inventory, 0, 23, 34, ItemStack::isEnchantable));
        SlotGenerator.begin(this::addSlot, 8, 84).playerInventory(playerInventory);

        this.addServerboundMessage(CurseMessage.class, this::executeCurse);
    }

    public void executeCurse(CurseMessage message) {
        if (this.context != ScreenHandlerContext.EMPTY) {
            var curses = Registries.ENCHANTMENT.stream()
                    .filter(Enchantment::isCursed)
                    .filter(Enchantment::isAvailableForRandomSelection)
                    .toList();

            var selectedCurse = Util.getRandom(curses, Random.create(this.player().getEnchantmentTableSeed()));
            this.inventory.getStack(0).addEnchantment(selectedCurse, Math.round(selectedCurse.getMaxLevel() * message.level / 3f));

            this.player().applyEnchantmentCosts(this.inventory.getStack(0), message.level);
        } else {
            this.sendMessage(message);
        }
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.dropInventory(player, this.inventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ScreenUtils.handleSlotTransfer(this, slot, 0);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, AffinityBlocks.OUIJA_BOARD);
    }

    public record CurseMessage(int level) {}
}
