package io.wispforest.affinity;

import blue.endless.jankson.Jankson;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.util.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AffinityConfig extends ConfigWrapper<io.wispforest.affinity.AffinityConfigModel> {

    public final Keys keys = new Keys();

    private final Option<java.lang.Boolean> renderEntitiesInStereopticonSectionImprints = this.optionForKey(this.keys.renderEntitiesInStereopticonSectionImprints);
    private final Option<java.lang.Boolean> renderBlockEntitiesInStereopticonSectionImprints = this.optionForKey(this.keys.renderBlockEntitiesInStereopticonSectionImprints);

    private AffinityConfig() {
        super(io.wispforest.affinity.AffinityConfigModel.class);
    }

    private AffinityConfig(Consumer<Jankson.Builder> janksonBuilder) {
        super(io.wispforest.affinity.AffinityConfigModel.class, janksonBuilder);
    }

    public static AffinityConfig createAndLoad() {
        var wrapper = new AffinityConfig();
        wrapper.load();
        return wrapper;
    }

    public static AffinityConfig createAndLoad(Consumer<Jankson.Builder> janksonBuilder) {
        var wrapper = new AffinityConfig(janksonBuilder);
        wrapper.load();
        return wrapper;
    }

    public boolean renderEntitiesInStereopticonSectionImprints() {
        return renderEntitiesInStereopticonSectionImprints.value();
    }

    public void renderEntitiesInStereopticonSectionImprints(boolean value) {
        renderEntitiesInStereopticonSectionImprints.set(value);
    }

    public boolean renderBlockEntitiesInStereopticonSectionImprints() {
        return renderBlockEntitiesInStereopticonSectionImprints.value();
    }

    public void renderBlockEntitiesInStereopticonSectionImprints(boolean value) {
        renderBlockEntitiesInStereopticonSectionImprints.set(value);
    }


    public static class Keys {
        public final Option.Key renderEntitiesInStereopticonSectionImprints = new Option.Key("renderEntitiesInStereopticonSectionImprints");
        public final Option.Key renderBlockEntitiesInStereopticonSectionImprints = new Option.Key("renderBlockEntitiesInStereopticonSectionImprints");
    }
}

