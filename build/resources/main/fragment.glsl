#version 330

in vec3 interpolatedTexCoords;
in float interpolatedShadingValues;
out vec4 fragColor;

uniform sampler2DArray textureArraySampler;

void main() {
    fragColor = texture(textureArraySampler, interpolatedTexCoords) * interpolatedShadingValues;
}