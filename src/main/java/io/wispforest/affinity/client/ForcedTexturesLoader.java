package io.wispforest.affinity.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.wispforest.affinity.Affinity;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.moddata.ModDataLoader;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;

public class ForcedTexturesLoader implements ModDataConsumer {

    private static final ForcedTexturesLoader INSTANCE = new ForcedTexturesLoader();

    private ForcedTexturesLoader() {}

    @Override
    public String getDataSubdirectory() {
        return "force_loaded_textures";
    }

    @Override
    public void acceptParsedFile(Identifier id, JsonObject object) {
        final var textures = new ArrayList<Identifier>();

        try {
            var atlasId = JsonHelper.getString(object, "atlas", null);
            if (atlasId == null) {
                Affinity.LOGGER.warn("Forced texture definition '{}' is missing 'atlas' entry", id);
                return;
            }

            var textureIds = JsonHelper.getArray(object, "textures", new JsonArray());

            for (var texture : textureIds) {
                if (!texture.isJsonPrimitive() || !texture.getAsJsonPrimitive().isString()) continue;
                textures.add(new Identifier(texture.getAsString()));
            }

            ClientSpriteRegistryCallback.event(new Identifier(atlasId)).register((atlasTexture, registry) -> textures.forEach(registry::register));
        } catch (Exception e) {
            Affinity.LOGGER.warn("Caught error whilst trying to read forced texture definition '{}'", id, e);
        }
    }

    public static void load() {
        ModDataLoader.load(INSTANCE);
    }
}
