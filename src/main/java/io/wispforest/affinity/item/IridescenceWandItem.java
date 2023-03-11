package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.template.LinkableBlockEntity;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class IridescenceWandItem extends Item implements DirectInteractionHandler {

    public static final NbtKey<Mode> MODE = new NbtKey<>("Mode", NbtKey.Type.STRING.then(Mode::byId, mode -> mode.id));
    public static final NbtKey<NbtCompound> LINK_DATA = new NbtKey<>("LinkData", NbtKey.Type.COMPOUND);

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
            playerStack.delete(LINK_DATA);
        } else {
            playerStack.mutate(MODE, Mode::next);
        }

        return TypedActionResult.success(playerStack);
    }

    @Override
    public boolean shouldHandleInteraction(World world, BlockPos pos, BlockState state) {
        return world.getBlockEntity(pos) instanceof LinkableBlockEntity;
    }

    @Override
    public Text getName(ItemStack stack) {
        final var mode = stack.get(MODE);

        return Text.translatable(this.getTranslationKey()).append(Text.translatable(
                WAND_OF_IRIDESCENCE_PREFIX + ".mode_suffix",
                TextOps.translateWithColor(WAND_OF_IRIDESCENCE_PREFIX + ".mode." + mode.id, mode.color)
        ));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        final var mode = stack.get(MODE);

        tooltip.add(Text.empty());

        tooltip.add(TextOps.translateWithColor(WAND_OF_IRIDESCENCE_PREFIX + ".mode." + mode.id, mode.color)
                .append(TextOps.withFormatting(": ", Formatting.GRAY))
                .append(Text.translatable(WAND_OF_IRIDESCENCE_PREFIX + ".mode." + mode.id + ".description")));

        tooltip.add(Text.translatable(WAND_OF_IRIDESCENCE_PREFIX + ".help.template",
                Text.translatable(WAND_OF_IRIDESCENCE_PREFIX + ".help")).setStyle(Style.EMPTY.withColor(mode.color)));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var stack = context.getStack();
        final var world = context.getWorld();
        final var pos = context.getBlockPos();
        final var mode = stack.get(MODE);

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
            stack.put(LINK_DATA, data);

            return ActionResult.SUCCESS;
        } else {
            var result = mode == Mode.BIND
                    ? linkable.finishLink(context.getPlayer(), storedPos, stack.get(LINK_DATA))
                    : linkable.destroyLink(context.getPlayer(), storedPos, stack.get(LINK_DATA));

            stack.delete(LINK_DATA);
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
        return stack.has(LINK_DATA) ?
                BlockPos.fromLong(stack.get(LINK_DATA).getLong("Position"))
                : null;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.has(LINK_DATA);
    }

    public enum Mode {
        BIND(0x721B26),
        RELEASE(0x11667C);

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

        public static Mode byId(String id) {
            return "release".equals(id) ? RELEASE : BIND;
        }
    }
}
