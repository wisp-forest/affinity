package io.wispforest.affinity.misc.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class ExperienceUtil {

    // generally useful constant
    public static final int POINTS_30_LEVELS = toPoints(30);

    // used in toLevels(...)
    private static final int POINTS_16_LEVELS = toPoints(16);
    private static final int POINTS_31_LEVELS = toPoints(31);

    public static int totalPoints(PlayerEntity player) {
        return toPoints(player.experienceLevel) + (int) (player.experienceProgress * player.getNextLevelExperience());
    }

    public static void updateExperience(ServerPlayerEntity player, int totalPoints) {
        player.setExperienceLevel(toLevels(totalPoints));
        player.setExperiencePoints(totalPoints - toPoints(player.experienceLevel));
    }

    public static int toPoints(int levels) {
        if (levels <= 16) {
            return levels * levels + 6 * levels;
        } else if (levels <= 31) {
            return (int) (2.5f * levels * levels - 40.5f * levels + 360);
        } else {
            return (int) (4.5f * levels * levels - 164.5f * levels + 2220);
        }
    }

    public static int toLevels(int points) {
        if (points <= POINTS_16_LEVELS) {
            return (int) (Math.sqrt(points + 9) - 3);
        } else if (points <= POINTS_31_LEVELS) {
            return (int) ((Math.sqrt(40 * points - 7839) + 81) / 10);
        } else {
            return (int) ((Math.sqrt(72 * points - 54215) + 325) / 18);
        }
    }

}
