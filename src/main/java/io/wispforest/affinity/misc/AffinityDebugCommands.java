package io.wispforest.affinity.misc;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.affinity.object.AffinityPoiTypes;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.poi.PointOfInterestStorage;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AffinityDebugCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("find_ritual_cores")
                    .then(argument("radius", IntegerArgumentType.integer())
                            .executes(AffinityDebugCommands::executeFindCores)));
        });
    }

    private static int executeFindCores(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();

        var cores = ((ServerWorld) player.world).getPointOfInterestStorage().getInCircle(type -> type == AffinityPoiTypes.RITUAL_CORE,
                player.getBlockPos(), IntegerArgumentType.getInteger(context, "radius"), PointOfInterestStorage.OccupationStatus.ANY).toList();

        player.sendMessage(Text.of("Found " + cores.size() + " cores"), false);

        for (var core : cores) {
            player.sendMessage(Text.of("-> [" + core.getPos().getX() + " " + core.getPos().getY() + " " + core.getPos().getZ() + "]"), false);
        }

        return cores.size();
    }
}
