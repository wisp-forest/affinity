package io.wispforest.affinity.misc;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.storage.AethumFluxContainer;
import io.wispforest.affinity.component.AethumComponent;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.PlayerAethumComponent;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AffinityDebugCommands {

    public static final int AFFINITY_COLOR = 0x94B3FD;
    public static final int VALUE_COLOR = 0x2FDD92;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
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
                                    .then(literal("get")
                                            .then(getPlayerAethumNode(PlayerAethumType.VALUE))
                                            .then(getPlayerAethumNode(PlayerAethumType.MAX))
                                            .then(getPlayerAethumNode(PlayerAethumType.REGEN)))
                                    .then(literal("set")
                                            .then(setPlayerAethumNode(PlayerAethumType.VALUE))
                                            .then(setPlayerAethumNode(PlayerAethumType.MAX))
                                            .then(setPlayerAethumNode(PlayerAethumType.REGEN))))));

            dispatcher.register(literal("aethumflux")
                    .then(argument("position", BlockPosArgumentType.blockPos())
                            .then(literal("get").executes(AffinityDebugCommands::getAethumFluxAt))
                            .then(literal("set")
                                    .then(argument("flux", LongArgumentType.longArg(0)).executes(AffinityDebugCommands::setAethumFluxAt)))));
        });
    }

    private static int setAethumFluxAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "position");
        long flux = LongArgumentType.getLong(context, "flux");

        BlockEntity be = context.getSource().getWorld().getBlockEntity(pos);

        if (be instanceof AethumFluxContainer container) {
            container.updateFlux(flux);
            context.getSource().sendFeedback(simpleFeedback("block flux updated"), true);
        }

        return 0;
    }

    private static int getAethumFluxAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "position");

        BlockEntity be = context.getSource().getWorld().getBlockEntity(pos);

        if (be instanceof AethumFluxContainer container) {
            context.getSource().sendFeedback(valueFeedback("block flux", container.flux()), true);

            return (int) container.flux();
        }

        return 0;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> setPlayerAethumNode(PlayerAethumType type) {
        return literal(type.name().toLowerCase()).then(argument("aethum", DoubleArgumentType.doubleArg())
                .executes(context -> setPlayerAethum(context, type)));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> getPlayerAethumNode(PlayerAethumType type) {
        return literal(type.name().toLowerCase())
                .executes(context -> getPlayerAethum(context, type));
    }

    private static int regenerateChunkAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        AffinityComponents.CHUNK_AETHUM.get(context.getSource().getPlayer().world
                .getChunk(BlockPosArgumentType.getBlockPos(context, "chunk"))).regenerate();

        context.getSource().sendFeedback(simpleFeedback("chunk aethum regenerated"), true);

        return 0;
    }

    private static int setChunkAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var pos = BlockPosArgumentType.getBlockPos(context, "chunk");
        final var aethum = DoubleArgumentType.getDouble(context, "aethum");

        AffinityComponents.CHUNK_AETHUM.get(player.world.getChunk(pos)).setAethum(aethum);
        context.getSource().sendFeedback(simpleFeedback("chunk aethum updated"), true);

        return (int) Math.round(aethum);
    }

    private static int getChunkAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var pos = BlockPosArgumentType.getBlockPos(context, "chunk");

        final double chunkAethum = AffinityComponents.CHUNK_AETHUM.get(player.world.getChunk(pos)).getAethum();
        context.getSource().sendFeedback(valueFeedback("chunk aethum", chunkAethum), true);

        return (int) Math.round(chunkAethum);
    }

    private static int getWorldAethum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var pos = BlockPosArgumentType.getBlockPos(context, "position");

        final double worldAethum = AffinityComponents.CHUNK_AETHUM.get(context.getSource().getWorld().getChunk(pos)).aethumAt(pos.getX(), pos.getZ());
        context.getSource().sendFeedback(valueFeedback("world aethum", worldAethum), true);

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

    private static int getPlayerAethum(CommandContext<ServerCommandSource> context, PlayerAethumType type) throws CommandSyntaxException {
        final double playerAethum = type.getter.apply(AffinityComponents.PLAYER_AETHUM.get(context.getSource().getPlayer()));
        context.getSource().sendFeedback(valueFeedback("player " + type.displayName, playerAethum), true);

        return (int) Math.round(playerAethum);
    }

    private static int setPlayerAethum(CommandContext<ServerCommandSource> context, PlayerAethumType type) throws CommandSyntaxException {
        final var aethum = DoubleArgumentType.getDouble(context, "aethum");
        type.setter.accept(AffinityComponents.PLAYER_AETHUM.get(EntityArgumentType.getPlayer(context, "player")), aethum);

        context.getSource().sendFeedback(simpleFeedback("player " + type.displayName + " updated"), true);

        return (int) Math.round(aethum);
    }

    private static Text simpleFeedback(String message) {
        return TextOps.withColor("affinity §> " + message, AFFINITY_COLOR, TextOps.color(Formatting.GRAY));
    }

    private static Text valueFeedback(String message, Object value) {
        return TextOps.withColor("affinity §> " + message + ": §" + value, AFFINITY_COLOR, TextOps.color(Formatting.GRAY), VALUE_COLOR);
    }

    private enum PlayerAethumType {
        VALUE("aethum", PlayerAethumComponent::getAethum, AethumComponent::setAethum),
        MAX("max aethum", PlayerAethumComponent::getMaxAethum, PlayerAethumComponent::setMaxAethum),
        REGEN("aethum regen speed", PlayerAethumComponent::getNaturalRegenSpeed, PlayerAethumComponent::setNaturalRegenSpeed);

        public final String displayName;
        public final Function<PlayerAethumComponent, Double> getter;
        public final BiConsumer<PlayerAethumComponent, Double> setter;

        PlayerAethumType(String displayName, Function<PlayerAethumComponent, Double> getter, BiConsumer<PlayerAethumComponent, Double> setter) {
            this.displayName = displayName;
            this.getter = getter;
            this.setter = setter;
        }
    }

}
