#version 150

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec4 ColorModulator;
uniform float GuiScale;

out vec4 fragColor;

void main() {
    vec2 samplePos = vec2(
        (GuiScale * floor(gl_FragCoord.x / GuiScale) + .5) / InputResolution.x,
        (GuiScale * floor(gl_FragCoord.y / GuiScale) + .5) / InputResolution.y
    );

    vec4 col = vec4(0);
    col += 0.37487566 * texture(InputSampler, samplePos + vec2(-0.75777156, -0.75777156) / InputResolution);
    col += 0.37487566 * texture(InputSampler, samplePos + vec2(0.75777156, -0.75777156) / InputResolution);
    col += 0.37487566 * texture(InputSampler, samplePos + vec2(0.75777156, 0.75777156) / InputResolution);
    col += 0.37487566 * texture(InputSampler, samplePos + vec2(-0.75777156, 0.75777156) / InputResolution);

    col += -0.12487566 * texture(InputSampler, samplePos + vec2(-2.90709914, 0.0) / InputResolution);
    col += -0.12487566 * texture(InputSampler, samplePos + vec2(2.90709914, 0.0) / InputResolution);
    col += -0.12487566 * texture(InputSampler, samplePos + vec2(0.0, -2.90709914) / InputResolution);
    col += -0.12487566 * texture(InputSampler, samplePos + vec2(0.0, 2.90709914) / InputResolution);

    fragColor = col;
}
