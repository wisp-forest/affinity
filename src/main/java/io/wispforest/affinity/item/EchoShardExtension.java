package io.wispforest.affinity.item;

import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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

import java.util.List;

public class EchoShardExtension {

    public static final NbtKey.Type<BlockPos> BLOCK_POS_TYPE = NbtKey.Type.of(
            NbtElement.LONG_TYPE,
            (nbt, s) -> BlockPos.fromLong(nbt.getLong(s)),
            (nbt, s, pos) -> nbt.putLong(s, pos.asLong())
    );

    public static final NbtKey<BlockPos> POS = new NbtKey<>("Pos", BLOCK_POS_TYPE);
    public static final NbtKey<Identifier> WORLD = new NbtKey<>("World", NbtKey.Type.IDENTIFIER);
    public static final NbtKey<Boolean> BOUND = new NbtKey<>("Bound", NbtKey.Type.BOOLEAN);

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

}
