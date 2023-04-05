package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.block.impl.RitualSocleBlock;
import io.wispforest.affinity.item.SocleOrnamentItem;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.ValidatingSlot;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class RitualSocleComposerScreenHandler extends ScreenHandler {

    public static final int ORNAMENT_INGREDIENT_SLOT = 0;
    public static final int ORNAMENT_CRAFT_SLOT = 1;
    public static final int SOCLE_SLOT = 2;
    public static final int ORNAMENT_INPUT_SLOT = 3;
    public static final int BLANK_SOCLE_INPUT_SLOT = 4;
    public static final int ORNAMENT_OUTPUT_SLOT = 5;
    public static final int BLANK_SOCLE_OUTPUT_SLOT = 6;
    public static final int INVENTORY_SHIFT = -2;

    private final ScreenHandlerContext context;
    private final Inventory inventory;
    private final CraftingInventory craftingInventory;
    private final CraftingResultInventory resultInventory;

    public static RitualSocleComposerScreenHandler client(int syncId, PlayerInventory inventory) {
        return new RitualSocleComposerScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public RitualSocleComposerScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(AffinityScreenHandlerTypes.RITUAL_SOCLE_COMPOSER, syncId);
        this.context = context;
        this.inventory = new SimpleInventory(5);

        this.craftingInventory = new CraftingInventory(this, 1, 1);
        this.resultInventory = new CraftingResultInventory();

        this.addSlot(new Slot(craftingInventory, ORNAMENT_INGREDIENT_SLOT, 26, 62));
        this.addSlot(new OrnamentResultSlot(playerInventory.player, this.craftingInventory, this.resultInventory,
                ORNAMENT_CRAFT_SLOT - 1, 26, 20));

        this.addSlot(new ValidatingSlot(this.inventory, SOCLE_SLOT + INVENTORY_SHIFT, 107, 40,
                stack -> stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof RitualSocleBlock));

        this.addSlot(new ValidatingSlot(this.inventory, ORNAMENT_INPUT_SLOT + INVENTORY_SHIFT, 71, 20,
                stack -> stack.getItem() instanceof SocleOrnamentItem));
        this.addSlot(new ValidatingSlot(this.inventory, BLANK_SOCLE_INPUT_SLOT + INVENTORY_SHIFT, 71, 60,
                stack -> stack.isOf(AffinityBlocks.BLANK_RITUAL_SOCLE.asItem())));

        this.addSlot(new ValidatingSlot(this.inventory, ORNAMENT_OUTPUT_SLOT + INVENTORY_SHIFT, 143, 20, stack -> false));
        this.addSlot(new ValidatingSlot(this.inventory, BLANK_SOCLE_OUTPUT_SLOT + INVENTORY_SHIFT, 143, 60, stack -> false));

        SlotGenerator.begin(this::addSlot, 8, 93).playerInventory(playerInventory);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.context.run((world, blockPos) -> {
            if (world.isClient()) return;

            var recipe = world.getRecipeManager()
                    .getFirstMatch(AffinityRecipeTypes.ORNAMENT_CARVING, inventory, world);

            final var resultStack = recipe.isEmpty() ? ItemStack.EMPTY : recipe.get().getOutput(world.getRegistryManager());
            this.resultInventory.setStack(0, resultStack);
            this.setPreviousTrackedSlot(ORNAMENT_CRAFT_SLOT, resultStack);

            ((ServerPlayerEntity) ((PlayerInventory) this.getSlot(25).inventory).player).networkHandler
                    .sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), ORNAMENT_CRAFT_SLOT, resultStack));
        });

    }

    private void handleMergeRequest() {
        final var ornamentSlot = this.getSlot(ORNAMENT_INPUT_SLOT);
        final var blankSocleSlot = this.getSlot(BLANK_SOCLE_INPUT_SLOT);
        final var socleSlot = this.getSlot(SOCLE_SLOT);

        if (!canMerge(ornamentSlot.getStack(), blankSocleSlot.getStack(), socleSlot.getStack())) return;

        this.incrementOrSet(socleSlot, RitualSocleType.forItem(ornamentSlot.getStack()).socleBlock().asItem().getDefaultStack());

        if (!ItemOps.emptyAwareDecrement(blankSocleSlot.getStack())) blankSocleSlot.setStack(ItemStack.EMPTY);
        if (!ItemOps.emptyAwareDecrement(ornamentSlot.getStack())) ornamentSlot.setStack(ItemStack.EMPTY);
    }

    public static boolean canMerge(ItemStack ornament, ItemStack blankSocle, ItemStack socle) {
        final var socleType = RitualSocleType.forItem(ornament);
        if (socleType == null) return false;

        return !blankSocle.isEmpty() && ItemOps.canStack(socle, socleType.socleBlock().asItem().getDefaultStack());
    }

    private void handleSplitRequest() {
        final var socleSlot = this.getSlot(SOCLE_SLOT);
        final var ornamentSlot = this.getSlot(ORNAMENT_OUTPUT_SLOT);
        final var blankSocleSlot = this.getSlot(BLANK_SOCLE_OUTPUT_SLOT);

        if (!canSplit(socleSlot.getStack(), ornamentSlot.getStack(), blankSocleSlot.getStack())) return;

        this.incrementOrSet(ornamentSlot, RitualSocleType.forBlockItem(socleSlot.getStack()).ornamentItem().getDefaultStack());
        this.incrementOrSet(blankSocleSlot, AffinityBlocks.BLANK_RITUAL_SOCLE.asItem().getDefaultStack());
        if (!ItemOps.emptyAwareDecrement(socleSlot.getStack())) socleSlot.setStack(ItemStack.EMPTY);
    }

    public static boolean canSplit(ItemStack socle, ItemStack resultOrnament, ItemStack blankSocle) {
        final var socleType = RitualSocleType.forBlockItem(socle);
        if (socleType == null) return false;

        return ItemOps.canStack(resultOrnament, socleType.ornamentItem().getDefaultStack())
                && ItemOps.canStack(blankSocle, AffinityBlocks.BLANK_RITUAL_SOCLE.asItem().getDefaultStack());
    }

    public ItemStack itemAt(int index) {
        return this.getSlot(index).getStack();
    }

    private void incrementOrSet(Slot slot, ItemStack stack) {
        if (slot.hasStack()) {
            slot.getStack().increment(1);
            slot.markDirty();
        } else {
            slot.setStack(stack);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, AffinityBlocks.RITUAL_SOCLE_COMPOSER);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {

        if (index == ORNAMENT_CRAFT_SLOT) {
            final var slot = this.slots.get(index);
            var slotStack = slot.getStack();
            var slotStackCopy = slotStack.copy();

            if (!this.insertItem(slotStack, BLANK_SOCLE_OUTPUT_SLOT + 1, BLANK_SOCLE_OUTPUT_SLOT + 1 + 36, true)) {
                return ItemStack.EMPTY;
            }

            slot.onQuickTransfer(slotStack, slotStackCopy);

            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            slot.onTakeItem(player, slotStack);
            return slotStackCopy.getCount() == slotStack.getCount() ? ItemStack.EMPTY : slotStackCopy;
        } else {
            return ScreenUtils.handleSlotTransfer(this, index, this.inventory.size() + 2);
        }

    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> {
            this.dropInventory(player, this.inventory);
            this.dropInventory(player, this.craftingInventory);
        });
    }

    public static void initNetwork() {
        AffinityNetwork.CHANNEL.registerServerbound(ActionRequestPacket.class, (message, access) -> {
            if (!(access.player().currentScreenHandler instanceof RitualSocleComposerScreenHandler composerHandler)) return;
            switch (message.action()) {
                case REQUEST_MERGE -> composerHandler.handleMergeRequest();
                case REQUEST_SPLIT -> composerHandler.handleSplitRequest();
            }
        });
    }

    public static class OrnamentResultSlot extends CraftingResultSlot {

        private final PlayerEntity player;
        private final CraftingInventory input;

        public OrnamentResultSlot(PlayerEntity player, CraftingInventory input, Inventory inventory, int index, int x, int y) {
            super(player, input, inventory, index, x, y);
            this.player = player;
            this.input = input;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            DefaultedList<ItemStack> defaultedList = player.world.getRecipeManager().getRemainingStacks(AffinityRecipeTypes.ORNAMENT_CARVING,
                    this.input, player.world);

            for (int i = 0; i < defaultedList.size(); ++i) {
                ItemStack itemStack = this.input.getStack(i);
                ItemStack itemStack2 = defaultedList.get(i);
                if (!itemStack.isEmpty()) {
                    this.input.removeStack(i, 1);
                    itemStack = this.input.getStack(i);
                }

                if (!itemStack2.isEmpty()) {
                    if (itemStack.isEmpty()) {
                        this.input.setStack(i, itemStack2);
                    } else if (ItemStack.areItemsEqual(itemStack, itemStack2) && ItemStack.areNbtEqual(itemStack, itemStack2)) {
                        itemStack2.increment(itemStack.getCount());
                        this.input.setStack(i, itemStack2);
                    } else if (!this.player.getInventory().insertStack(itemStack2)) {
                        this.player.dropItem(itemStack2, false);
                    }
                }
            }
        }
    }

    public record ActionRequestPacket(Action action) {}

    public enum Action {
        REQUEST_MERGE,
        REQUEST_SPLIT
    }
}
