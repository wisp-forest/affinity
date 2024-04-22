package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.template.LinkableBlockEntity;
import io.wispforest.affinity.endec.nbt.NbtEndec;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class IridescenceWandItem extends Item implements DirectInteractionHandler {

    public static final KeyedEndec<Mode> MODE_KEY = Mode.ENDEC.keyed("Mode", Mode.BIND);
    public static final KeyedEndec<NbtCompound> LINK_DATA_KEY = NbtEndec.COMPOUND.keyed("LinkData", (NbtCompound) null);
    public static final KeyedEndec<Boolean> RETAIN_MODE_KEY = Endec.BOOLEAN.keyed("RetainMode", false);

    private static final String WAND_OF_IRIDESCENCE_PREFIX = "item.affinity.wand_of_iridescence";

    public IridescenceWandItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        final var playerStack = user.getStackInHand(hand);
        if (!user.isSneaking()) return TypedActionResult.pass(playerStack);

        if (world.isClient) return TypedActionResult.success(playerStack);
        if (this.getStoredPos(playerStack) != null) {
            playerStack.delete(LINK_DATA_KEY);
        } else {
            playerStack.mutate(MODE_KEY, Mode::next);
        }

        return TypedActionResult.success(playerStack);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT) return false;

        stack.mutate(RETAIN_MODE_KEY, enabled -> !enabled);
        return true;
    }

    @Override
    public boolean shouldHandleInteraction(ItemStack stack, World world, BlockPos pos, BlockState state) {
        return world.getBlockEntity(pos) instanceof LinkableBlockEntity;
    }

    @Override
    public Text getName(ItemStack stack) {
        final var mode = stack.get(MODE_KEY);

        return Text.translatable(this.getTranslationKey()).append(Text.translatable(
                WAND_OF_IRIDESCENCE_PREFIX + ".mode_suffix",
                TextOps.translateWithColor(WAND_OF_IRIDESCENCE_PREFIX + ".mode." + mode.id, mode.color)
        ));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        final var mode = stack.get(MODE_KEY);

        tooltip.add(Text.empty());

        tooltip.add(TextOps.translateWithColor(WAND_OF_IRIDESCENCE_PREFIX + ".mode." + mode.id, mode.color)
                .append(TextOps.withFormatting(": ", Formatting.GRAY))
                .append(Text.translatable(WAND_OF_IRIDESCENCE_PREFIX + ".mode." + mode.id + ".description")));

        tooltip.add(Text.translatable(WAND_OF_IRIDESCENCE_PREFIX + ".help.template",
                Text.translatable(WAND_OF_IRIDESCENCE_PREFIX + ".help")).setStyle(Style.EMPTY.withColor(mode.color)));

        tooltip.add(Text.empty());
        tooltip.add(Text.translatable(WAND_OF_IRIDESCENCE_PREFIX + ".retain_mode." + (stack.get(RETAIN_MODE_KEY) ? "enabled" : "disabled")).styled(style -> style.withColor(mode.color)));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var stack = context.getStack();
        final var world = context.getWorld();
        final var pos = context.getBlockPos();
        final var mode = stack.get(MODE_KEY);

        if (!(world.getBlockEntity(pos) instanceof LinkableBlockEntity linkable)) return ActionResult.PASS;
        var blockName = world.getBlockState(pos).getBlock().getName();

        var storedPos = this.getStoredPos(stack);
        if (Objects.equals(storedPos, context.getBlockPos())) return ActionResult.PASS;

        if (storedPos == null) {
            var data = new NbtCompound();
            var result = linkable.beginLink(context.getPlayer(), data);
            if (result.isPresent()) {
                context.getPlayer().sendMessage(Text.translatable(result.get(), blockName), true);
                return ActionResult.SUCCESS;
            }

            data.putLong("Position", pos.asLong());
            stack.put(LINK_DATA_KEY, data);

            return ActionResult.SUCCESS;
        } else {
            var result = mode == Mode.BIND
                    ? linkable.finishLink(context.getPlayer(), storedPos, stack.get(LINK_DATA_KEY))
                    : linkable.destroyLink(context.getPlayer(), storedPos, stack.get(LINK_DATA_KEY));

            if (!stack.get(RETAIN_MODE_KEY)) stack.delete(LINK_DATA_KEY);

            if (result.isPresent()) {
                result.map(LinkableBlockEntity.LinkResult::messageTranslationKey).ifPresentOrElse(translationKey -> {
                    context.getPlayer().sendMessage(Text.translatable(translationKey, blockName), true);
                }, () -> {
                    context.getPlayer().playSound(AffinitySoundEvents.ITEM_IRIDESCENCE_WAND_BIND, 1, .75f + world.random.nextFloat() * .5f);
                });

                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        }
    }

    public BlockPos getStoredPos(ItemStack stack) {
        return stack.has(LINK_DATA_KEY) ?
                BlockPos.fromLong(stack.get(LINK_DATA_KEY).getLong("Position"))
                : null;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.has(LINK_DATA_KEY);
    }

    public enum Mode {
        BIND(0x721B26),
        RELEASE(0x11667C);

        public static final Endec<Mode> ENDEC = Endec.STRING.xmap(id -> "release".equals(id) ? RELEASE : BIND, mode -> mode.id);

        public final String id;
        public final int color;

        Mode(int color) {
            this.id = this.name().toLowerCase(Locale.ROOT);
            this.color = color;
        }

        public Mode next() {
            return switch (this) {
                case RELEASE -> BIND;
                case BIND -> RELEASE;
            };
        }
    }
}
