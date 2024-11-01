package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.potion.ExtraPotionData;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.affinity.particle.BezierPathEmitterParticleEffect;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EchoShardExtension {

    public static final ComponentType<BoundLocation> COMPONENT = Affinity.component("bound_location", BoundLocation.ENDEC);

    public static void apply() {
        // TODO: move this out into a misc class
        ExtraPotionData.mark(COMPONENT);

        if (Affinity.onClient()) {
            ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
                if (!stack.isOf(Items.ECHO_SHARD)) return;

                var location = stack.get(COMPONENT);
                if (location == null) return;

                formatLocationTooltip(location, lines);
            });

            //noinspection Convert2MethodRef
            ClientTickEvents.END_WORLD_TICK.register(world -> EchoShardExtension.displayParticles(world));
        }

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            final var stack = player.getStackInHand(hand);
            if (!stack.isOf(Items.ECHO_SHARD) || !player.shouldCancelInteraction()) return ActionResult.PASS;

            return handleBlockUse(world, stack, hitResult.getBlockPos().up());
        });
    }

    @Environment(EnvType.CLIENT)
    private static void displayParticles(ClientWorld world) {
        if (world.random.nextFloat() > .2) return;

        var player = MinecraftClient.getInstance().player;
        for (var hand : Hand.values()) {
            var stack = player.getStackInHand(hand);

            if (!stack.isOf(Items.ECHO_SHARD)) return;

            var location = stack.get(COMPONENT);

            if (location == null || !location.world().equals(MinecraftClient.getInstance().world.getRegistryKey().getValue())) {
                continue;
            }

            ClientParticles.spawn(
                    new BezierPathEmitterParticleEffect(ParticleTypes.PORTAL, Vec3d.ofCenter(location.pos()), 30, 10, false),
                    world,
                    Vec3d.ofCenter(location.pos(), -.5),
                    1.5
            );
        }
    }

    public static ActionResult handleBlockUse(World world, ItemStack stack, BlockPos pos) {
        stack.set(COMPONENT, new BoundLocation(pos, world.getRegistryKey().getValue()));

        if (world.isClient) {
            ClientParticles.setParticleCount(4);
            ClientParticles.randomizeVelocityOnAxis(.5, Direction.Axis.Y);
            ClientParticles.spawnCubeOutline(ParticleTypes.REVERSE_PORTAL, world, Vec3d.of(pos).add(.25, .25, .25), .5f, .01f);
        }

        WorldOps.playSound(world, pos, AffinitySoundEvents.ITEM_ECHO_SHARD_BIND, SoundCategory.PLAYERS, 1, 0);

        return ActionResult.SUCCESS;
    }

    public static void formatLocationTooltip(BoundLocation location, List<Text> tooltip) {
        var pos = location.pos();

        var worldText = TextOps.translateWithColor(Util.createTranslationKey("dimension", location.world()), 0x4D4C7D);
        var coordinateText = TextOps.withFormatting(pos.getX() + " " + pos.getY() + " " + pos.getZ(), Formatting.GRAY);

        tooltip.add(Text.translatable("text.affinity.echo_shard_location", coordinateText, worldText).formatted(Formatting.DARK_GRAY));
    }

    public static @Nullable BlockPos tryGetLocationInWorld(World world, ComponentHolder shard) {
        var location = shard.get(COMPONENT);
        if (location == null) return null;
        if (!location.world().equals(world.getRegistryKey().getValue())) return null;
        return location.pos();
    }

    public record BoundLocation(BlockPos pos, Identifier world) {
        public static final Endec<BoundLocation> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.BLOCK_POS.fieldOf("pos", BoundLocation::pos),
            MinecraftEndecs.IDENTIFIER.fieldOf("world", BoundLocation::world),
            BoundLocation::new
        );
    }
}
