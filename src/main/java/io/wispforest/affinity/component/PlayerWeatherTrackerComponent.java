package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerWeatherTrackerComponent implements TransientComponent, ServerTickingComponent, CopyableComponent<PlayerWeatherTrackerComponent> {
    private final ServerPlayerEntity player;

    private float rainGradient;
    private float thunderGradient;
    private boolean hasTicked = false;

    public PlayerWeatherTrackerComponent(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity spe)
            this.player = spe;
        else
            this.player = null;
    }

    @Override
    public void serverTick() {
        assert player != null;

        var chunkWeather = AffinityComponents.LOCAL_WEATHER.get(player.world.getChunk(player.getBlockPos()));

        if (!hasTicked) {
            hasTicked = true;

            rainGradient = chunkWeather.getRainGradient();
            thunderGradient = chunkWeather.getThunderGradient();
        }

        float prevRainGradient = rainGradient;
        float prevThunderGradient = thunderGradient;

        boolean wasRaining = rainGradient != 0;

        rainGradient += Math.signum(chunkWeather.getRainGradient() - rainGradient) * 0.01f;
        thunderGradient += Math.signum(chunkWeather.getThunderGradient() - thunderGradient) * 0.01f;

        if (wasRaining && rainGradient == 0) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, 0));
        } else if (!wasRaining && rainGradient != 0) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STOPPED, 0));
        }

        if (rainGradient != prevRainGradient) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, rainGradient));
        }

        if (thunderGradient != prevThunderGradient) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, thunderGradient));
        }
    }

    @Override
    public void copyFrom(PlayerWeatherTrackerComponent other) {
        this.rainGradient = other.rainGradient;
        this.thunderGradient = other.thunderGradient;
    }
}
