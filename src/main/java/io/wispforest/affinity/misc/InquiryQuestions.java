package io.wispforest.affinity.misc;

import io.wispforest.affinity.Affinity;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class InquiryQuestions {

    private static final List<String> INQUIRY_QUESTIONS = new ArrayList<>();

    public static void initialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new Loader());
    }

    public static String question() {
        return INQUIRY_QUESTIONS.get(ThreadLocalRandom.current().nextInt(INQUIRY_QUESTIONS.size()));
    }

    private static class Loader implements SynchronousResourceReloader, IdentifiableResourceReloadListener {

        @Override
        public void reload(ResourceManager manager) {
            manager.getResource(Affinity.id("inquiry.questions")).ifPresent(resource -> {
                INQUIRY_QUESTIONS.clear();
                try (var stream = resource.getInputStream()) {
                    Collections.addAll(INQUIRY_QUESTIONS, IOUtils.toString(stream, StandardCharsets.UTF_8).split("\n"));
                } catch (IOException e) {
                    Affinity.LOGGER.error("Could not load inquiry questions", e);
                }
            });
        }

        @Override
        public Identifier getFabricId() {
            return Affinity.id("inquiry_questions");
        }
    }

}
