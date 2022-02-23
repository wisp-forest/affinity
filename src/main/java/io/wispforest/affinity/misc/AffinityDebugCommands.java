package io.wispforest.affinity.misc;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.misc.components.AffinityComponents;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
                                    .then(literal("regenerate").executes(AffinityDebugCommands::regenerateChunkAethum)))));

        });
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

        final var message = TextOps.withColor("affinity §> chunk aethum: §" + chunkAethum,
                AFFINITY_COLOR, TextOps.color(Formatting.GRAY), VALUE_COLOR);
        context.getSource().sendFeedback(message, true);

        return (int) Math.round(chunkAethum);
    }

    private static Text simpleFeedback(String message) {
        return TextOps.withColor("affinity §> " + message, AFFINITY_COLOR, TextOps.color(Formatting.GRAY));
    }

}
