#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 texCoords;
layout (location = 2) in float shadingValues;

out vec3 interpolatedTexCoords;
out float interpolatedShadingValues;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    interpolatedTexCoords = texCoords;
    interpolatedShadingValues = shadingValues;
}