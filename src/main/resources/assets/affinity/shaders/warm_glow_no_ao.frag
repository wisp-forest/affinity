#include frex:shaders/api/fragment.glsl
#include frex:shaders/lib/math.glsl
#include lumi:shaders/api/pbr_ext.glsl

void frx_startFragment(inout frx_FragmentData fragData) {
    float e = frx_luminance(fragData.spriteColor.rgb);
    bool lit = e >  0.8 || (fragData.spriteColor.r - fragData.spriteColor.b) > 0.3f;
    fragData.emissivity = lit ? e : 0.0;
    fragData.diffuse = fragData.diffuse && !lit;
    fragData.ao = false;

    #ifdef LUMI_PBRX
        pbr_roughness = 0.4;// make it smooth
    #endif
}