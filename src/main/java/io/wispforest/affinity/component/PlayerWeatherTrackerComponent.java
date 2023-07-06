package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PlayerWeatherTrackerComponent implements TransientComponent, ServerTickingComponent, CopyableComponent<PlayerWeatherTrackerComponent> {
    @NotNull
    private final ServerPlayerEntity player;

    private float rainGradient;
    private float thunderGradient;
    private float syncedRainGradient = -1;
    private float syncedThunderGradient = -1;
    private boolean hasTicked = false;

    public PlayerWeatherTrackerComponent(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            this.player = serverPlayer;
        } else {
            //noinspection ConstantConditions
            this.player = null;
        }
    }

    @Override
    public void serverTick() {
        var chunkWeather = AffinityComponents.LOCAL_WEATHER.get(player.getWorld().getChunk(player.getBlockPos()));

        if (!this.hasTicked) {
            this.hasTicked = true;

            this.rainGradient = chunkWeather.getRainGradient();
            this.thunderGradient = chunkWeather.getThunderGradient();
        }

        this.rainGradient += Math.signum(chunkWeather.getRainGradient() - this.rainGradient) * 0.01f;
        this.thunderGradient += Math.signum(chunkWeather.getThunderGradient() - this.thunderGradient) * 0.01f;

        if (this.rainGradient != this.syncedRainGradient) {
            if (this.syncedRainGradient == 0) {
                player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, 0));
            } else if (this.rainGradient == 0) {
                player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STOPPED, 0));
            }

            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, rainGradient));
            this.syncedRainGradient = this.rainGradient;
        }

        if (this.thunderGradient != this.syncedThunderGradient) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, thunderGradient));
            this.syncedThunderGradient = this.thunderGradient;
        }
    }

    @Override
    public void copyFrom(PlayerWeatherTrackerComponent other) {
        this.rainGradient = other.rainGradient;
        this.thunderGradient = other.thunderGradient;
    }
}
