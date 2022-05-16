package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.util.NbtKey;
import io.wispforest.owo.util.VectorSerializer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EchoShardItem extends Item {
    public static final NbtKey<Vec3d> POS = new NbtKey<>("Pos", NbtKey.Type.of(NbtElement.LIST_TYPE, VectorSerializer::get, (tag, s, vec3d) -> VectorSerializer.store(vec3d, tag, s)));
    public static final NbtKey<Identifier> WORLD = new NbtKey<>("World", NbtKey.Type.of(NbtElement.STRING_TYPE, (tag, s) -> new Identifier(tag.getString(s)), (tag, s, id) -> tag.putString(s, id.toString())));

    public EchoShardItem() {
        super(AffinityItems.settings(0));
    }

    public static void formatLocationTooltip(NbtCompound data, List<Text> tooltip) {
        var pos = POS.get(data);
        var targetWorld = WORLD.get(data);
        var worldText = new TranslatableText("dimension." + targetWorld.getNamespace() + "." + targetWorld.getPath());

        tooltip.add(new TranslatableText("text.affinity.echo_shard_location", worldText, (int)pos.x, (int)pos.y, (int)pos.z));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        POS.put(stack.getOrCreateNbt(), user.getPos());
        WORLD.put(stack.getOrCreateNbt(), world.getRegistryKey().getValue());

        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt() && stack.getNbt().contains("Pos", NbtElement.LIST_TYPE)) {
            formatLocationTooltip(stack.getNbt(), tooltip);
        }
    }
}
