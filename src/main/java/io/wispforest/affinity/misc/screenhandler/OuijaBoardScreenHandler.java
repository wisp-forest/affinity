package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.client.screen.OuijaBoardScreen;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.SyncedProperty;
import io.wispforest.owo.client.screens.ValidatingSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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

    private final Enchantment[] currentCurses = new Enchantment[3];
    private final SyncedProperty<Integer> seed;

    public static OuijaBoardScreenHandler client(int syncId, PlayerInventory playerInventory) {
        return new OuijaBoardScreenHandler(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public OuijaBoardScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(AffinityScreenHandlerTypes.OUIJA_BOARD, syncId);
        this.context = context;

        this.addSlot(new ValidatingSlot(this.inventory, 0, 23, 34, ItemStack::isEnchantable));
        SlotGenerator.begin(this::addSlot, 8, 84).playerInventory(playerInventory);

        this.addServerboundMessage(CurseMessage.class, this::executeCurse);

        this.seed = this.createProperty(int.class, -1);
        this.seed.observe(integer -> {
            this.updateCurses();

            if (this.context != ScreenHandlerContext.EMPTY) return;
            this.updateScreen();
        });

        this.seed.set(playerInventory.player.getEnchantmentTableSeed());
    }

    public void executeCurse(CurseMessage message) {
        if (this.context != ScreenHandlerContext.EMPTY) {
            var selectedCurse = this.currentCurses[message.level - 1];
            this.inventory.getStack(0).addEnchantment(selectedCurse, 1);

            this.player().applyEnchantmentCosts(this.inventory.getStack(0), message.level);
            this.seed.set(this.player().getEnchantmentTableSeed());
        } else {
            this.sendMessage(message);
        }
    }

    private void updateCurses() {
        var curses = Registries.ENCHANTMENT.stream()
                .filter(Enchantment::isCursed)
                .filter(Enchantment::isAvailableForRandomSelection)
                .toList();

        var random = Random.create(this.seed.get());
        this.currentCurses[0] = Util.getRandom(curses, random);
        this.currentCurses[1] = Util.getRandom(curses, random);
        this.currentCurses[2] = Util.getRandom(curses, random);
    }

    @Environment(EnvType.CLIENT)
    private void updateScreen() {
        if (!(MinecraftClient.getInstance().currentScreen instanceof OuijaBoardScreen screen)) return;
        screen.updateCurses();
    }

    public Enchantment[] currentCurses() {
        return this.currentCurses;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.dropInventory(player, this.inventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ScreenUtils.handleSlotTransfer(this, slot, this.inventory.size());
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, AffinityBlocks.OUIJA_BOARD);
    }

    public record CurseMessage(int level) {}
}
