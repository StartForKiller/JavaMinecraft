#version 330

in vec3 interpolatedTexCoords;
in float interpolatedShadingValues;
out vec4 fragColor;

uniform sampler2DArray textureArraySampler;

void main() {
    vec4 textureColour = texture(textureArraySampler, interpolatedTexCoords);
    fragColor = textureColour * interpolatedShadingValues;

    if(textureColour.a == 0.0) {
        discard;
    }
}