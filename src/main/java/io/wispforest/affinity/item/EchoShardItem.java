package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EchoShardItem extends Item {
    public EchoShardItem() {
        super(AffinityItems.settings(0));
    }

    public static void formatLocationTooltip(NbtCompound data, List<Text> tooltip) {
        var pos = NbtHelper.toBlockPos(data.getCompound("Pos"));
        var targetWorld = new Identifier(data.getString("World"));
        var worldText = new TranslatableText("dimension." + targetWorld.getNamespace() + "." + targetWorld.getPath());

        tooltip.add(new TranslatableText("text.affinity.echo_shard_location", worldText, pos.toShortString()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        stack.getOrCreateNbt().put("Pos", NbtHelper.fromBlockPos(user.getBlockPos()));
        stack.getOrCreateNbt().putString("World", world.getRegistryKey().getValue().toString());

        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt() && stack.getNbt().contains("Pos", NbtElement.COMPOUND_TYPE)) {
            formatLocationTooltip(stack.getNbt(), tooltip);
        }
    }
}
