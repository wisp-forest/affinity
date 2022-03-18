package io.wispforest.affinity.mixin.access;

import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureFeature.class)
public interface StructureFeatureAccessor {

    @Invoker("register")
    static <F extends StructureFeature<?>> F affinity$register(String name, F structureFeature, GenerationStep.Feature step) {
        throw new AssertionError("wat");
    }

}
