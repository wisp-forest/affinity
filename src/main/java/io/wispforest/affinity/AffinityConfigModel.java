package io.wispforest.affinity;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;

@Modmenu(modId = "affinity")
@Config(name = "affinity", wrapperName = "AffinityConfig")
public class AffinityConfigModel {

    public boolean renderEntitiesInStereopticonSectionImprints = true;
    public boolean renderBlockEntitiesInStereopticonSectionImprints = true;

    @RangeConstraint(min = 0, max = 10)
    public int stereopticonSectionImprintRecursionLimit = 0;

}
