package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.screen.OuijaBoardScreen;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.SyncedProperty;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;
import java.util.stream.Collectors;

public class OuijaBoardScreenHandler extends ScreenHandler {

    private static final TagKey<Enchantment> NOT_AVAILABLE_IN_OUIJA_BOARD = TagKey.of(RegistryKeys.ENCHANTMENT, Affinity.id("not_available_in_ouija_board"));

    private final SimpleInventory inventory = new SimpleInventory(1);
    private final ScreenHandlerContext context;
    private final PlayerEntity player;

    @SuppressWarnings("unchecked")
    private final RegistryEntry<Enchantment>[] currentCurses = new RegistryEntry[3];
    private final SyncedProperty<Integer> seed;

    public static OuijaBoardScreenHandler client(int syncId, PlayerInventory playerInventory) {
        return new OuijaBoardScreenHandler(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public OuijaBoardScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(AffinityScreenHandlerTypes.OUIJA_BOARD, syncId);
        this.context = context;

        this.addSlot(new Slot(this.inventory, 0, 23, 36) {
            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });
        SlotGenerator.begin(this::addSlot, 8, 86).playerInventory(playerInventory);

        this.addServerboundMessage(CurseMessage.class, this::executeCurse);

        this.seed = this.createProperty(int.class, -1);
        this.seed.observe(integer -> this.updateCurses());
        this.inventory.addListener(sender -> this.updateCurses());

        this.player = playerInventory.player;
        this.seed.set(playerInventory.player.getEnchantmentTableSeed());
    }

    public void executeCurse(CurseMessage message) {
        var selectedCurse = this.currentCurses[message.level - 1];

        int cost = this.enchantmentCost(selectedCurse);
        if (!this.canAfford(cost)) return;

        this.player().applyEnchantmentCosts(this.inventory.getStack(0), cost);

        if (this.context != ScreenHandlerContext.EMPTY) {
            var stack = this.inventory.getStack(0);
            if (stack.isOf(Items.BOOK)) {
                var enchantedBook = Items.ENCHANTED_BOOK.getDefaultStack();

                var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                builder.add(selectedCurse, 1);
                enchantedBook.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());

                this.inventory.setStack(0, enchantedBook);
            } else {
                stack.addEnchantment(selectedCurse, 1);
            }

            this.seed.set(this.player().getEnchantmentTableSeed());

            this.context.run((world, blockPos) -> {
                world.getChunk(blockPos).getComponent(AffinityComponents.CHUNK_AETHUM).tryConsumeAethum(5);
            });
        } else {
            this.sendMessage(message);
        }
    }

    public int enchantmentCost(RegistryEntry<Enchantment> curse) {
        int baseCost = curse.value().getAnvilCost();
        return baseCost + this.inventory.getStack(0).getEnchantments().getEnchantments().size() * 4;
    }

    public boolean canAfford(int levels) {
        return this.player().isCreative() || this.player().experienceLevel >= levels;
    }

    private void updateCurses() {
        var stack = this.inventory.getStack(0);

        var curses = this.player.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).streamEntries()
                .filter(x -> x.isIn(EnchantmentTags.CURSE))
                .filter(enchantment -> !enchantment.isIn(NOT_AVAILABLE_IN_OUIJA_BOARD))
                .filter(enchantment -> enchantment.value().isAcceptableItem(stack) || stack.isOf(Items.BOOK))
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

    public RegistryEntry<Enchantment>[] currentCurses() {
        return this.currentCurses;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, this.inventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        if (slot > 0) {
            var clickedSlot = this.slots.get(slot);
            if (!clickedSlot.hasStack()) return ItemStack.EMPTY;

            var clickedStack = clickedSlot.getStack();
            if (this.slots.get(0).hasStack()) return ItemStack.EMPTY;

            this.slots.get(0).setStack(ItemOps.singleCopy(clickedSlot.getStack()));

            if (!ItemOps.emptyAwareDecrement(clickedStack)) {
                clickedSlot.setStack(ItemStack.EMPTY);
            } else {
                clickedSlot.markDirty();
            }

            return clickedStack;
        } else {
            return ScreenUtils.handleSlotTransfer(this, slot, this.inventory.size());
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, AffinityBlocks.OUIJA_BOARD);
    }

    public record CurseMessage(int level) {}
}
