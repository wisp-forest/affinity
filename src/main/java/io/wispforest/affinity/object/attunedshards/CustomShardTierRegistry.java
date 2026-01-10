package io.wispforest.affinity.object.attunedshards;

import com.google.gson.*;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.util.FuturesUtil;
import io.wispforest.endec.format.gson.GsonDeserializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.Item;

import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CustomShardTierRegistry {

    public static HashMap<Item, AttunedShardTier> REGISTRY = new HashMap<>();

    public static void initialize() {
        Affinity.LOGGER.info("Affinity shard tier reload listener registered.");
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new Loader());
    }

    private static void registerItems(List<Item> itemList, AttunedShardTier tier) {
        Affinity.LOGGER.info("Affinity registering items under tier {}", tier.toString());
        REGISTRY.putAll(itemList.stream().collect(Collectors.toMap(item -> item, ign-> tier)));
    }

    public static void registerItems(CustomShardTierJsonFile jsonFile) {
        registerItems(jsonFile.getItems(), jsonFile.getTier());

    }

    private static class Loader implements IdentifiableResourceReloadListener {

        @Override
        public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
            CompletableFuture.supplyAsync(() -> {
                    CustomShardTierRegistry.REGISTRY.clear();
                    return null;
                }, prepareExecutor).thenCompose(synchronizer::whenPrepared);

            return FuturesUtil.allOf( manager.findResources(
                    "tiers",
                    path -> path.toString().endsWith(".json")
            ).entrySet().parallelStream().map(ent ->
                CompletableFuture.runAsync(
                    () -> {
                        try (BufferedReader reader = ent.getValue().getReader()) {
                            try {
                                JsonElement json = JsonParser.parseReader(reader);
                                if (json.isJsonNull()) {
                                    return;
                                }

                                CustomShardTierJsonFile result = CustomShardTierJsonFile.ENDEC.decodeFully(GsonDeserializer::of, json);
                                CustomShardTierRegistry.registerItems(result);

                            } catch (JsonParseException e) {
                                Affinity.LOGGER.error("Parsing failed", e);
                            }
                        } catch (IOException | NoSuchElementException e) {
                            Affinity.LOGGER.error("Couldn't open resource described by {}", ent.getKey(), e);
                        }
                    }, applyExecutor
                )).toList()
            ).thenRun( () -> {
                /* INVARIANTS */
                if (CustomShardTierRegistry.REGISTRY.isEmpty()) {
                    Affinity.LOGGER.debug("Affinity Shard registry reloaded with no json entries.");
                    CustomShardTierRegistry.REGISTRY.put(Items.AMETHYST_SHARD, AttunedShardTiers.CRUDE);
                }
            });
        }

        @Override
        public String getName() {
            return Affinity.MOD_ID;
        }

        @Override
        public Identifier getFabricId() {
            return Affinity.id("tiers");
        }
    }
}
