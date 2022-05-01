package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumLink.Element;
import io.wispforest.affinity.aethumflux.net.AethumLink.Type;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.util.NbtKey;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class IridescenceWandItem extends Item implements DirectInteractionHandler {

    public static final NbtKey<String> MODE = new NbtKey<>("Mode", NbtKey.Type.STRING);
    public static final NbtKey<NbtCompound> LINK_DATA = new NbtKey<>("LinkData", NbtKey.Type.COMPOUND);

    private static final String WAND_OF_IRIDESCENCE_PREFIX = "item.affinity.wand_of_iridescence";

    public IridescenceWandItem() {
        super(AffinityItems.settings(0).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        final var playerStack = user.getStackInHand(hand);
        if (!user.isSneaking()) return TypedActionResult.pass(playerStack);

        if (this.getStoredPos(playerStack) != null) return TypedActionResult.pass(playerStack);
        if (world.isClient) return TypedActionResult.success(playerStack);

        final var stackNbt = playerStack.getOrCreateNbt();
        final var mode = Mode.byId(MODE.get(stackNbt));

        MODE.put(stackNbt, mode.next().id);

        return TypedActionResult.success(playerStack);
    }

    @Override
    public boolean shouldHandleInteraction(World world, BlockPos pos, BlockState state) {
        return Affinity.AETHUM_MEMBER.find(world, pos, null) != null;
    }

    @Override
    public Text getName(ItemStack stack) {
        final var mode = Mode.byId(MODE.get(stack.getOrCreateNbt()));

        return new TranslatableText(this.getTranslationKey()).append(new TranslatableText(WAND_OF_IRIDESCENCE_PREFIX + ".mode.template",
                TextOps.translateWithColor(WAND_OF_IRIDESCENCE_PREFIX + ".mode." + mode.id, mode.color)).formatted(Formatting.GRAY));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        final var mode = Mode.byId(MODE.get(stack.getOrCreateNbt()));

        tooltip.add(Text.of(" "));

        tooltip.add(TextOps.translateWithColor(WAND_OF_IRIDESCENCE_PREFIX + ".mode." + mode.id, mode.color)
                .append(new LiteralText(": ").formatted(Formatting.GRAY))
                .append(new TranslatableText(WAND_OF_IRIDESCENCE_PREFIX + ".mode." + mode.id + ".description").formatted(Formatting.GRAY)));

        tooltip.add(new TranslatableText(WAND_OF_IRIDESCENCE_PREFIX + ".help.template",
                new TranslatableText(WAND_OF_IRIDESCENCE_PREFIX + ".help").formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withColor(mode.color)));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var stack = context.getStack();
        final var world = context.getWorld();
        final var pos = context.getBlockPos();

        final var mode = Mode.byId(MODE.get(stack.getOrCreateNbt()));

        var nextMember = Affinity.AETHUM_MEMBER.find(world, pos, null);
        if (nextMember == null) return ActionResult.PASS;
        if (!nextMember.acceptsLinks()) {
            final var isNode = nextMember instanceof AethumNetworkNode;
            if (mode == Mode.BIND && isNode) {
                context.getPlayer().sendMessage(new TranslatableText(AethumLink.Result.TOO_MANY_LINKS.translationKey), true);
                LINK_DATA.delete(stack.getOrCreateNbt());
                return ActionResult.SUCCESS;
            } else if (!isNode) {
                return ActionResult.PASS;
            }
        }

        if (Objects.equals(getStoredPos(stack), context.getBlockPos())) return ActionResult.PASS;
        var existingElement = getLink(stack);

        if (existingElement == null) {
            var linkType = context.getPlayer().isSneaking() ? nextMember.specialLinkType() : Type.NORMAL;

            beginLink(stack, pos, Element.of(nextMember), linkType);

            context.getPlayer().sendMessage(new TranslatableText(mode == Mode.BIND ? linkType.translationKey : mode.initTranslationKey,
                    world.getBlockState(pos).getBlock().getName()), true);
            return ActionResult.SUCCESS;
        }

        if (existingElement == Element.MEMBER && Element.of(nextMember) == Element.MEMBER) {
            return ActionResult.PASS;
        }

        var result = mode.processor.run(stack, world, existingElement, pos, nextMember);
        context.getPlayer().sendMessage(new TranslatableText(result.translationKey, world.getBlockState(pos).getBlock().getName()), true);

        LINK_DATA.delete(stack.getOrCreateNbt());

        return ActionResult.SUCCESS;
    }

    private BlockPos getStoredPos(ItemStack stack) {
        return stack.getOrCreateNbt().contains("LinkData", NbtElement.COMPOUND_TYPE) ?
                BlockPos.fromLong(stack.getOrCreateNbt().getCompound("LinkData").getLong("Position"))
                : null;
    }

    private Element getLink(ItemStack stack) {
        var nbt = stack.getOrCreateNbt();
        if (!LINK_DATA.isIn(nbt)) return null;

        return Element.values()[LINK_DATA.get(nbt).getInt("Element")];
    }

    private void beginLink(ItemStack stack, BlockPos pos, Element element, Type type) {
        var nbt = new NbtCompound();
        nbt.putLong("Position", pos.asLong());
        nbt.putInt("Element", element.ordinal());
        nbt.putInt("Type", type.ordinal());

        LINK_DATA.put(stack.getOrCreateNbt(), nbt);
    }

    private static AethumLink.Result executeBind(ItemStack stack, World world, @NotNull Element existingElement, BlockPos nextPos, AethumNetworkMember nextMember) {
        var linkNbt = LINK_DATA.get(stack.getOrCreateNbt());
        var existingPos = BlockPos.fromLong(linkNbt.getLong("Position"));
        var linkType = Type.values()[linkNbt.getInt("Type")];

        if (existingElement == Element.NODE) {
            var node = Affinity.AETHUM_NODE.find(world, existingPos, null);
            return node == null ? AethumLink.Result.NO_TARGET : node.createGenericLink(nextPos, linkType);
        } else {
            return ((AethumNetworkNode) nextMember).createGenericLink(existingPos, linkType);
        }
    }

    private static AethumLink.Result executeRelease(ItemStack stack, World world, @NotNull Element existingElement, BlockPos nextPos, AethumNetworkMember nextMember) {
        var linkNbt = LINK_DATA.get(stack.getOrCreateNbt());
        var existingPos = BlockPos.fromLong(linkNbt.getLong("Position"));

        if (existingElement == Element.NODE) {
            var node = Affinity.AETHUM_NODE.find(world, existingPos, null);
            return node == null ? AethumLink.Result.NO_TARGET : node.destroyLink(nextPos);
        } else {
            return ((AethumNetworkNode) nextMember).destroyLink(existingPos);
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return LINK_DATA.maybeIsIn(stack.getNbt());
    }

    public enum Mode {
        BIND(0x721B26, IridescenceWandItem::executeBind, null),
        RELEASE(0x11667C, IridescenceWandItem::executeRelease, "message.affinity.linking.started_destroy");

        public final String id;
        public final int color;
        public final MemberProcessor processor;
        public final String initTranslationKey;

        Mode(int color, MemberProcessor processor, String initTranslationKey) {
            this.id = this.name().toLowerCase(Locale.ROOT);
            this.color = color;
            this.processor = processor;
            this.initTranslationKey = initTranslationKey;
        }

        public Mode next() {
            return Mode.values()[(this.ordinal() + 1) % Mode.values().length];
        }

        public static Mode byId(String id) {
            return "release".equals(id) ? RELEASE : BIND;
        }
    }

    @FunctionalInterface
    private interface MemberProcessor {
        AethumLink.Result run(ItemStack stack, World world, @NotNull Element existingElement, BlockPos nextPos, AethumNetworkMember nextMember);
    }
}
