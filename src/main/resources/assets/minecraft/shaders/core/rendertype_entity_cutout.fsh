#version 150

#moj_import <fog.glsl>
#moj_import <emissive_utils.glsl>

#define IS_CUSTOM_EMISSIVE is_emissive(Sampler0) // If texture should have emissive

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (is_emissive(Sampler0)) {
        vec4 textureProperties = get_texture_properties(Sampler0);
        vec4 lmc = get_light(textureProperties, color, lightMapColor);
        color.a = get_alpha(textureProperties, color);

        if (color.a < 0.1) {
            discard;
        }
        color *= vertexColor * ColorModulator;
        color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
        color *= lmc;
    } else {
        if (color.a < 0.1) {
            discard;
        }
        color *= vertexColor * ColorModulator;
        color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
        color *= lightMapColor;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
