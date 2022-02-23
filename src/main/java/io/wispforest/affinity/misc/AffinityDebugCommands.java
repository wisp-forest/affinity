package io.wispforest.affinity.misc;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.misc.components.AethumComponent;
import io.wispforest.affinity.misc.components.AffinityComponents;
import io.wispforest.affinity.misc.components.PlayerAethumComponent;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AffinityDebugCommands {

    public static final int AFFINITY_COLOR = 0x94B3FD;
    public static final int VALUE_COLOR = 0x2FDD92;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("aethum")
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

        });
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

    private static Text valueFeedback(String message, double value) {
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
