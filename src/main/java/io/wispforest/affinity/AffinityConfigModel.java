package io.wispforest.affinity;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.RestartRequired;

@Modmenu(modId = "affinity")
@Config(name = "affinity", wrapperName = "AffinityConfig")
public class AffinityConfigModel {

    public boolean renderEntitiesInStereopticonSectionImprints = true;
    public boolean renderBlockEntitiesInStereopticonSectionImprints = true;

    @RangeConstraint(min = 0, max = 10)
    public int stereopticonSectionImprintRecursionLimit = 0;

    @RestartRequired
    @RangeConstraint(min = 3, max = 100)
    public int maxFluxNodeShards = 5;
}
