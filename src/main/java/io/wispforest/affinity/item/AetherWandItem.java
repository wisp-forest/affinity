package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.AetherLink;
import io.wispforest.affinity.blockentity.AetherLink.Element;
import io.wispforest.affinity.blockentity.AetherNetworkMember;
import io.wispforest.affinity.blockentity.AetherNetworkNode;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AetherWandItem extends Item {

    public AetherWandItem() {
        super(new Settings().group(Affinity.AFFINITY_GROUP).maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var stack = context.getStack();
        final var world = context.getWorld();
        final var pos = context.getBlockPos();

        var nextMember = Affinity.AETHER_MEMBER.find(world, pos, null);
        if (nextMember == null) return ActionResult.PASS;

        if (Objects.equals(getStoredPos(stack), context.getBlockPos())) return ActionResult.PASS;
        var existingElement = getLink(stack);

        if (existingElement == null) {
            beginLink(stack, pos, Element.of(nextMember));

            context.getPlayer().sendMessage(new TranslatableText("message.affinity.linking.started",
                    world.getBlockState(pos).getBlock().getName()), true);

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

        return Element.values()[nbt.getCompound("LinkingFrom").getInt("Type")];
    }

    private void beginLink(ItemStack stack, BlockPos pos, Element element) {
        var nbt = new NbtCompound();
        nbt.putLong("Position", pos.asLong());
        nbt.putInt("Type", element.ordinal());

        stack.getOrCreateNbt().put("LinkingFrom", nbt);
    }

    private AetherLink.Result executeLink(ItemStack stack, World world, @NotNull Element existingElement, BlockPos nextPos, AetherNetworkMember nextMember) {
        var linkNbt = stack.getOrCreateNbt().getCompound("LinkingFrom");
        var existingPos = BlockPos.fromLong(linkNbt.getLong("Position"));

        stack.getOrCreateNbt().remove("LinkingFrom");

        return switch (existingElement) {
            case NODE -> Affinity.AETHER_NODE.find(world, existingPos, null).createGenericLink(nextPos);
            case MEMBER -> ((AetherNetworkNode) nextMember).createGenericLink(existingPos);
        };
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.getOrCreateNbt().contains("LinkingFrom");
    }
}
