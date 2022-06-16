package io.wispforest.affinity.mixin.access;

import net.minecraft.world.gen.structure.StructureType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureType.class)
public interface StructureFeatureAccessor {

//    @Invoker("register")
//    static <F extends StructureType<?>> F affinity$register(String name, F structureFeature, GenerationStep.Feature step) {
//        throw new AssertionError("wat");
//    }

}
