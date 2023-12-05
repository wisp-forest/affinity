package io.wispforest.affinity.item;

import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import io.wispforest.owo.serialization.util.MapCarrier;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EchoShardExtension {

    public static final KeyedEndec<BlockPos> POS = BuiltInEndecs.BLOCK_POS.keyed("Pos", BlockPos.ORIGIN);
    public static final KeyedEndec<Identifier> WORLD = BuiltInEndecs.IDENTIFIER.keyed("World", DimensionTypes.OVERWORLD_ID);
    public static final KeyedEndec<Boolean> BOUND = Endec.BOOLEAN.keyed("Bound", false);

    public static void apply() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
                if (!stack.isOf(Items.ECHO_SHARD)) return;
                if (stack.has(BOUND)) {
                    formatLocationTooltip(stack.getNbt(), lines);
                }
            });
        }

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            final var stack = player.getStackInHand(hand);
            if (!stack.isOf(Items.ECHO_SHARD)) return ActionResult.PASS;

            return handleBlockUse(world, stack, hitResult.getBlockPos().up());
        });
    }

    public static ActionResult handleBlockUse(World world, ItemStack stack, BlockPos pos) {
        stack.put(BOUND, true);
        stack.put(POS, pos);
        stack.put(WORLD, world.getRegistryKey().getValue());

        if (world.isClient) {
            ClientParticles.setParticleCount(4);
            ClientParticles.randomizeVelocityOnAxis(.5, Direction.Axis.Y);
            ClientParticles.spawnCubeOutline(ParticleTypes.REVERSE_PORTAL, world, Vec3d.of(pos).add(.25, .25, .25), .5f, .01f);
        }

        WorldOps.playSound(world, pos, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.PLAYERS, 1, 0);

        return ActionResult.SUCCESS;
    }

    public static void formatLocationTooltip(NbtCompound data, List<Text> tooltip) {
        var pos = data.get(POS);
        var targetWorld = data.get(WORLD);

        var worldText = TextOps.translateWithColor(Util.createTranslationKey("dimension", targetWorld), 0x4D4C7D);
        var coordinateText = TextOps.withFormatting(pos.getX() + " " + pos.getY() + " " + pos.getZ(), Formatting.GRAY);

        tooltip.add(Text.translatable("text.affinity.echo_shard_location", coordinateText, worldText).formatted(Formatting.DARK_GRAY));
    }

    public static @Nullable BlockPos tryGetLocationInWorld(World world, MapCarrier shard) {
        if (!shard.get(BOUND)) return null;
        if (!shard.get(WORLD).equals(world.getRegistryKey().getValue())) return null;
        return shard.get(POS);
    }

}
