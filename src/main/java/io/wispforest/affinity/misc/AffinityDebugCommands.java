package io.wispforest.affinity.misc;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.aethumflux.storage.AethumFluxContainer;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.entity.EmancipatedBlockEntity;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AffinityDebugCommands {

    public static final int AFFINITY_COLOR = 0x94B3FD;
    public static final int VALUE_COLOR = 0x2FDD92;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            dispatcher.register(literal("emancipate").executes(context -> {
                var player = context.getSource().getPlayer();
                var targetPos = ((BlockHitResult) player.raycast(5, 0, false)).getBlockPos();

                var targetState = player.getWorld().getBlockState(targetPos);
                if (targetState.isAir()) return 0;

                EmancipatedBlockEntity.spawn(player.getWorld(), targetPos, targetState, player.getWorld().getBlockEntity(targetPos), 20, 0f);
                player.getWorld().removeBlock(targetPos, false);

                return 1;
            }));

            dispatcher.register(literal("debug-ethereal-data").executes(context -> {
                var storage = context.getSource()
                        .getWorld()
                        .getScoreboard()
                        .getComponent(AffinityComponents.ETHEREAL_NODE_STORAGE);

                var tag = new NbtCompound();
                storage.writeToNbt(tag, access);

                context.getSource().sendFeedback(() -> new NbtTextFormatter(" ").apply(tag), false);
                return 1;
            }));

            dispatcher.register(literal("aethum")
                    .then(literal("world")
                            .then(argument("position", BlockPosArgumentType.blockPos())
                                    .then(literal("get").executes(AffinityDebugCommands::getWorldAethum))
                                    .then(literal("dump")
                                            .then(argument("radius", IntegerArgumentType.integer()).executes(AffinityDebugCommands::dumpWorldAethum)
                                                    .then(argument("lower_bound", IntegerArgumentType.integer())
                                                            .then(argument("range", IntegerArgumentType.integer())
                                                                    .executes(AffinityDebugCommands::dumpWorldAethumBounded)))))))
                    .then(literal("chunk")
                            .then(argument("chunk", BlockPosArgumentType.blockPos())
                                    .then(literal("get").executes(AffinityDebugCommands::getChunkAethum))
                                    .then(literal("set")
                                            .then(argument("aethum", DoubleArgumentType.doubleArg()).executes(AffinityDebugCommands::setChunkAethum)))
                                    .then(literal("regenerate").executes(AffinityDebugCommands::regenerateChunkAethum))))
                    .then(literal("player")
                            .then(argument("player", EntityArgumentType.player())
                                    .then(literal("get").executes(AffinityDebugCommands::getPlayerAethum))
                                    .then(literal("set")
                                            .then(argument("aethum", DoubleArgumentType.doubleArg()).executes(AffinityDebugCommands::setPlayerAethum)))))
                    .then(literal("flux")
                            .then(argument("position", BlockPosArgumentType.blockPos())
                                    .then(literal("get").executes(AffinityDebugCommands::getAethumFluxAt))
                                    .then(literal("set")
                                            .then(argument("flux", LongArgumentType.longArg(0)).executes(AffinityDebugCommands::setAethumFluxAt))))));
        });
    }

    private static int setAethumFluxAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "position");
        long flux = LongArgumentType.getLong(context, "flux");

        BlockEntity be = context.getSource().getWorld().getBlockEntity(pos);

        if (be instanceof AethumFluxContainer container) {
            container.updateFlux(flux);
            context.getSource().sendFeedback(() -> simpleFeedback("block flux updated"), true);
        }

        return 0;
    }

    private static int getAethumFluxAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "position");

        BlockEntity be = context.getSource().getWorld().getBlockEntity(pos);

        if (be instanceof AethumFluxContainer container) {
            context.getSource().sendFeedback(() -> valueFeedback("block flux", container.flux()), true);

            return (int) container.flux();
        }

        return 0;
    }

    private static int regenerateChunkAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().getPlayer().getWorld().getChunk(BlockPosArgumentType.getBlockPos(context, "chunk"))
                .getComponent(AffinityComponents.CHUNK_AETHUM).regenerate();

        context.getSource().sendFeedback(() -> simpleFeedback("chunk aethum regenerated"), true);

        return 0;
    }

    private static int setChunkAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var pos = BlockPosArgumentType.getBlockPos(context, "chunk");
        final var aethum = DoubleArgumentType.getDouble(context, "aethum");

        player.getWorld().getChunk(pos).getComponent(AffinityComponents.CHUNK_AETHUM).setAethum(aethum);
        context.getSource().sendFeedback(() -> simpleFeedback("chunk aethum updated"), true);

        return (int) Math.round(aethum);
    }

    private static int getChunkAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var pos = BlockPosArgumentType.getBlockPos(context, "chunk");

        final double chunkAethum = player.getWorld().getChunk(pos).getComponent(AffinityComponents.CHUNK_AETHUM).getAethum();
        context.getSource().sendFeedback(() -> valueFeedback("chunk aethum", chunkAethum), true);

        return (int) Math.round(chunkAethum);
    }

    private static int getWorldAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var pos = BlockPosArgumentType.getBlockPos(context, "position");

        final double worldAethum = context.getSource().getWorld().getChunk(pos).getComponent(AffinityComponents.CHUNK_AETHUM).aethumAt(pos.getX(), pos.getZ());
        context.getSource().sendFeedback(() -> valueFeedback("world aethum", worldAethum), true);

        return (int) Math.round(worldAethum);
    }

    private static int dumpWorldAethumBounded(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return doWorldAethumDump(context, IntegerArgumentType.getInteger(context, "lower_bound"), IntegerArgumentType.getInteger(context, "range"));
    }

    private static int dumpWorldAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return doWorldAethumDump(context, 0, 100);
    }

    private static int doWorldAethumDump(CommandContext<ServerCommandSource> context, int lowerBound, int range) throws CommandSyntaxException {
        final var center = BlockPosArgumentType.getBlockPos(context, "position");
        final int radius = IntegerArgumentType.getInteger(context, "radius");
        final var world = context.getSource().getWorld();

        final var dumpFile = FabricLoader.getInstance().getGameDir().resolve("aethum_dump.png");
        final var image = new BufferedImage(radius + radius + 1, radius + radius + 1, BufferedImage.TYPE_INT_RGB);

        final var cache = AethumAcquisitionCache.forceLoadAndCreate(world,
                (center.getX() - radius) >> 4, (center.getZ() - radius) >> 4, MathHelper.ceilDiv(radius + radius, 16));

        try (var out = Files.newOutputStream(dumpFile)) {

            int zIdx, xIdx = 0;

            for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
                zIdx = 0;
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    var aethum = (int) (0xFF * ((cache.getComponentFrom(x >> 4, z >> 4).aethumAt(x, z) - lowerBound) / range));
                    aethum = MathHelper.clamp(aethum, 0, 0xFF);

                    image.setRGB(xIdx, zIdx++, aethum << 16 | aethum << 8 | aethum);
                }
                xIdx++;
            }

            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            context.getSource().sendError(Text.of(e.getMessage()));
            return 1;
        }

        return 0;
    }

    private static int getPlayerAethum(CommandContext<ServerCommandSource> context) {
        final double playerAethum = context.getSource().getPlayer().getComponent(AffinityComponents.PLAYER_AETHUM).getAethum();
        context.getSource().sendFeedback(() -> valueFeedback("player aethum", playerAethum), true);

        return (int) Math.round(playerAethum);
    }

    private static int setPlayerAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var aethum = DoubleArgumentType.getDouble(context, "aethum");
        EntityArgumentType.getPlayer(context, "player").getComponent(AffinityComponents.PLAYER_AETHUM).setAethum(aethum);

        context.getSource().sendFeedback(() -> simpleFeedback("player aethum updated"), true);

        return (int) Math.round(aethum);
    }

    private static Text simpleFeedback(String message) {
        return TextOps.withColor("affinity §> " + message, AFFINITY_COLOR, TextOps.color(Formatting.GRAY));
    }

    private static Text valueFeedback(String message, Object value) {
        return TextOps.withColor("affinity §> " + message + ": §" + value, AFFINITY_COLOR, TextOps.color(Formatting.GRAY), VALUE_COLOR);
    }

}
