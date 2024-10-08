#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MainDepthSampler;
uniform sampler2D SecondaryDepthSampler;

uniform vec4 ColorModulator;

in vec2 texCoord;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    if (texture(SecondaryDepthSampler, texCoord).x >= texture(MainDepthSampler, texCoord).x) {
        discard;
    }

    vec4 color = texture(DiffuseSampler, texCoord);

    // blit final output of compositor into displayed back buffer
    fragColor = color * ColorModulator;
}
