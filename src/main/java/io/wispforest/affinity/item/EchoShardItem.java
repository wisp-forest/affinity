package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.util.NbtKey;
import io.wispforest.owo.util.VectorSerializer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EchoShardItem extends Item {

    public static final NbtKey.Type<Vec3d> VEC_3D_TYPE = NbtKey.Type.of(NbtElement.LIST_TYPE, VectorSerializer::get, (tag, s, vec3d) -> VectorSerializer.store(vec3d, tag, s));
    public static final NbtKey.Type<Identifier> IDENTIFIER_TYPE = NbtKey.Type.of(NbtElement.STRING_TYPE, (tag, s) -> new Identifier(tag.getString(s)), (tag, s, id) -> tag.putString(s, id.toString()));

    public static final NbtKey<Vec3d> POS = new NbtKey<>("Pos", VEC_3D_TYPE);
    public static final NbtKey<Identifier> WORLD = new NbtKey<>("World", IDENTIFIER_TYPE);

    public EchoShardItem() {
        super(AffinityItems.settings(0));
    }

    public static void formatLocationTooltip(NbtCompound data, List<Text> tooltip) {
        var pos = POS.get(data);
        var targetWorld = WORLD.get(data);

        var worldText = TextOps.translateWithColor(Util.createTranslationKey("dimension", targetWorld), 0x4D4C7D);
        var coordinateText = TextOps.withFormatting((int) pos.x + " " + (int) pos.y + " " + (int) pos.z, Formatting.GRAY);

        tooltip.add(new TranslatableText("text.affinity.echo_shard_location", coordinateText, worldText).formatted(Formatting.DARK_GRAY));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var world = context.getWorld();
        final var stack = context.getStack();
        final var pos = context.getBlockPos().up();

        POS.put(stack.getOrCreateNbt(), Vec3d.ofCenter(pos));
        WORLD.put(stack.getOrCreateNbt(), world.getRegistryKey().getValue());

        if (world.isClient) {
            ClientParticles.setParticleCount(4);
            ClientParticles.randomizeVelocityOnAxis(.5, Direction.Axis.Y);
            ClientParticles.spawnCubeOutline(ParticleTypes.REVERSE_PORTAL, world, Vec3d.of(pos).add(.25, .25, .25), .5f, .01f);
        }

        WorldOps.playSound(world, pos, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.PLAYERS, 1, 0);

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt() && stack.getNbt().contains("Pos", NbtElement.LIST_TYPE)) {
            formatLocationTooltip(stack.getNbt(), tooltip);
        }
    }
}
