package io.wispforest.affinity.misc;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.entity.EmancipatedBlockEntity;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.util.hit.BlockHitResult;

import static net.minecraft.server.command.CommandManager.literal;

public class AffinityDebugCommands {

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
        });
    }
}
