package io.wispforest.affinity;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@Modmenu(modId = "affinity")
@Config(name = "affinity", wrapperName = "AffinityConfig")
public class AffinityConfigModel {

    public boolean renderEntitiesInStereopticonSectionImprints = true;
    public boolean renderBlockEntitiesInStereopticonSectionImprints = true;

    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public int affineInfuserCostPerDurabilityPoint = 50;

    @RestartRequired
    @RangeConstraint(min = 3, max = 127)
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public int maxFluxNodeShards = 5;

    // ---

    @SectionHeader("experimental")
    public boolean theSkyIrisIntegration = false;

    @RangeConstraint(min = 0, max = 10)
    public int stereopticonSectionImprintRecursionLimit = 0;

    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @RestartRequired
    public boolean unfinishedFeatures = false;
}
