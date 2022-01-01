package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumLink.Element;
import io.wispforest.affinity.aethumflux.net.AethumLink.Type;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IridescenceWandItem extends Item {

    public IridescenceWandItem() {
        super(new OwoItemSettings().tab(0).group(Affinity.AFFINITY_GROUP).maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var stack = context.getStack();
        final var world = context.getWorld();
        final var pos = context.getBlockPos();

        var nextMember = Affinity.AETHUM_MEMBER.find(world, pos, null);
        if (nextMember == null) return ActionResult.PASS;
        if (!nextMember.acceptsLinks()) return ActionResult.PASS;

        if (Objects.equals(getStoredPos(stack), context.getBlockPos())) return ActionResult.PASS;
        var existingElement = getLink(stack);

        if (existingElement == null) {
            var linkType = context.getPlayer().isSneaking() ? nextMember.specialLinkType() : Type.NORMAL;

            beginLink(stack, pos, Element.of(nextMember), linkType);

            context.getPlayer().sendMessage(new TranslatableText(linkType.translationKey, world.getBlockState(pos).getBlock().getName()), true);
            return ActionResult.SUCCESS;
        }

        if (existingElement == Element.MEMBER && Element.of(nextMember) == Element.MEMBER) {
            return ActionResult.PASS;
        }

        var result = executeLink(stack, world, existingElement, pos, nextMember);
        context.getPlayer().sendMessage(new TranslatableText(result.translationKey, world.getBlockState(pos).getBlock().getName()), true);

        return ActionResult.SUCCESS;
    }

    private BlockPos getStoredPos(ItemStack stack) {
        return stack.getOrCreateNbt().contains("LinkingFrom", NbtElement.COMPOUND_TYPE) ?
                BlockPos.fromLong(stack.getOrCreateNbt().getCompound("LinkingFrom").getLong("Position"))
                : null;
    }

    private Element getLink(ItemStack stack) {
        var nbt = stack.getOrCreateNbt();
        if (!nbt.contains("LinkingFrom", NbtElement.COMPOUND_TYPE)) return null;

        return Element.values()[nbt.getCompound("LinkingFrom").getInt("Element")];
    }

    private void beginLink(ItemStack stack, BlockPos pos, Element element, Type type) {
        var nbt = new NbtCompound();
        nbt.putLong("Position", pos.asLong());
        nbt.putInt("Element", element.ordinal());
        nbt.putInt("Type", type.ordinal());

        stack.getOrCreateNbt().put("LinkingFrom", nbt);
    }

    private AethumLink.Result executeLink(ItemStack stack, World world, @NotNull Element existingElement, BlockPos nextPos, AethumNetworkMember nextMember) {
        var linkNbt = stack.getOrCreateNbt().getCompound("LinkingFrom");
        var existingPos = BlockPos.fromLong(linkNbt.getLong("Position"));
        var linkType = Type.values()[linkNbt.getInt("Type")];

        stack.getOrCreateNbt().remove("LinkingFrom");

        if (existingElement == Element.NODE) {
            var node = Affinity.AETHUM_NODE.find(world, existingPos, null);
            return node == null ? AethumLink.Result.NO_TARGET : node.createGenericLink(nextPos, linkType);
        } else {
            return ((AethumNetworkNode) nextMember).createGenericLink(existingPos, linkType);
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.getOrCreateNbt().contains("LinkingFrom");
    }
}
