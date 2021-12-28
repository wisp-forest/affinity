package io.wispforest.affinity.mixin;

import net.minecraft.util.TopologicalSorts;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSource.class_6543;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(BiomeSource.class)
public class BiomeSourceMixin {

    @Redirect(method = "method_39525", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/TopologicalSorts;sort(Ljava/util/Map;Ljava/util/Set;Ljava/util/Set;Ljava/util/function/Consumer;Ljava/lang/Object;)Z"))
    private boolean printFeatures(Map<class_6543, Set<class_6543>> successors, Set<class_6543> visited, Set<class_6543> visiting, Consumer<class_6543> reversedOrderConsumer, Object now) {
        if (!TopologicalSorts.sort(successors, visited, visiting, reversedOrderConsumer, (class_6543) now)) return false;

        try (var writer = new BufferedWriter(new FileWriter("graph.dot", true))) {
            successors.forEach((featureData, class_6543s) -> {
                class_6543s.forEach(class_6543 -> {
                    var formatted = formatFeature(featureData) + " -> " + formatFeature(class_6543) + ";";
                    System.out.println(formatted);
                    try {
                        writer.write(formatted);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private static String formatFeature(class_6543 featureData) {
        return "\"" + featureData.feature()
                + " " + featureData.step() + " " + featureData.featureIndex() + '"';
    }
}
