package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.CompatMixin;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class AffinityMixinPlugin implements IMixinConfigPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            var mixinClass = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName);

            var compatAnnotation = Annotations.getInvisible(mixinClass, CompatMixin.class);
            if (compatAnnotation != null) {
                return FabricLoader.getInstance().isModLoaded(Annotations.getValue(compatAnnotation));
            }
        } catch (ClassNotFoundException | IOException ignored) {}

        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {}
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public List<String> getMixins() {
        return null;
    }
}
