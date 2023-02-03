package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.client.screen.OuijaBoardScreen;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.SyncedProperty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;
import java.util.stream.Collectors;

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

        this.addSlot(new Slot(this.inventory, 0, 23, 36));
        SlotGenerator.begin(this::addSlot, 8, 86).playerInventory(playerInventory);

        this.addServerboundMessage(CurseMessage.class, this::executeCurse);

        this.seed = this.createProperty(int.class, -1);
        this.seed.observe(integer -> this.updateCurses());
        this.inventory.addListener(sender -> this.updateCurses());

        this.seed.set(playerInventory.player.getEnchantmentTableSeed());
    }

    public void executeCurse(CurseMessage message) {
        var selectedCurse = this.currentCurses[message.level - 1];

        int cost = this.enchantmentCost(selectedCurse);
        if (!this.canAfford(cost)) return;

        this.player().applyEnchantmentCosts(this.inventory.getStack(0), cost);

        if (this.context != ScreenHandlerContext.EMPTY) {
            this.inventory.getStack(0).addEnchantment(selectedCurse, 1);
            this.seed.set(this.player().getEnchantmentTableSeed());
        } else {
            this.sendMessage(message);
        }
    }

    public int enchantmentCost(Enchantment curse) {
        int baseCost = switch (curse.getRarity()) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 4;
            case VERY_RARE -> 8;
        };

        return baseCost + this.inventory.getStack(0).getEnchantments().size() * 4;
    }

    public boolean canAfford(int levels) {
        return this.player().isCreative() || this.player().experienceLevel >= levels;
    }

    private void updateCurses() {
        var stack = this.inventory.getStack(0);

        var curses = Registries.ENCHANTMENT.stream()
                .filter(Enchantment::isCursed)
                .filter(Enchantment::isAvailableForRandomSelection)
                .filter(enchantment -> enchantment.isAcceptableItem(stack))
                .filter(enchantment -> EnchantmentHelper.getLevel(enchantment, stack) < 1)
                .collect(Collectors.toList());

        Arrays.fill(this.currentCurses, null);

        if (!curses.isEmpty()) {
            var random = Random.create(this.seed.get());

            for (int i = 0; i < 3; i++) {
                if (curses.isEmpty()) break;
                this.currentCurses[i] = curses.remove(random.nextInt(curses.size()));
            }
        }

        if (this.context != ScreenHandlerContext.EMPTY) return;
        this.updateScreen();
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
