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
        var chunkWeather = AffinityComponents.LOCAL_WEATHER.get(player.world.getChunk(player.getBlockPos()));

        if (!this.hasTicked) {
            this.hasTicked = true;

            this.rainGradient = chunkWeather.getRainGradient();
            this.thunderGradient = chunkWeather.getThunderGradient();
        }

        float prevRainGradient = this.rainGradient;
        float prevThunderGradient = this.thunderGradient;

        boolean wasRaining = this.rainGradient != 0;

        this.rainGradient += Math.signum(chunkWeather.getRainGradient() - this.rainGradient) * 0.01f;
        this.thunderGradient += Math.signum(chunkWeather.getThunderGradient() - this.thunderGradient) * 0.01f;

        if (wasRaining && this.rainGradient == 0) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, 0));
        } else if (!wasRaining && this.rainGradient != 0) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STOPPED, 0));
        }

        if (this.rainGradient != prevRainGradient) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, rainGradient));
        }

        if (this.thunderGradient != prevThunderGradient) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, thunderGradient));
        }
    }

    @Override
    public void copyFrom(PlayerWeatherTrackerComponent other) {
        this.rainGradient = other.rainGradient;
        this.thunderGradient = other.thunderGradient;
    }
}
