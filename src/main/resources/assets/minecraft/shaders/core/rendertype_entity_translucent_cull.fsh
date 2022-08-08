#version 150

#moj_import <fog.glsl>
#moj_import <emissive_utils.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    vec4 vtc = vertexColor;

    if (is_emissive(Sampler0)) {
        vec4 textureProperties = get_texture_properties(Sampler0);
        vtc = get_light(textureProperties, color, vertexColor);
        color.a = get_alpha(textureProperties, color);
    }

    color *= vtc * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
