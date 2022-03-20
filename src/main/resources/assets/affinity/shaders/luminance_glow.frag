

#include frex:shaders/api/fragment.glsl
#include frex:shaders/lib/math.glsl

/******************************************************
  canvas:shaders/material/luminance_glow.frag
******************************************************/

void frx_materialFragment() {
    #ifndef DEPTH_PASS
    	float e = frx_luminance(frx_sampleColor.rgb);
    	frx_fragEmissive = e * e;
    #endif

    #ifdef PBR_ENABLED
            frx_fragRoughness = 0.4;
    #endif

}