#version 150

#moj_import <fog.glsl>

#define IS_CUSTOM_EMISSIVE texelFetch(Sampler0, ivec2(0, 1), 0) == vec4(1, 0, 1, 1) // If texture should have emissive

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

bool check_alpha(float textureAlpha, float targetAlpha) {

    float targetLess = targetAlpha - 0.01;
    float targetMore = targetAlpha + 0.01;
    return (textureAlpha > targetLess && textureAlpha < targetMore);

}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (IS_CUSTOM_EMISSIVE) {
        /*
            Format:
            (x, y): DESC         (r, g, b, a)
            ---------------------------------
            (0, 1): Marker pixel (1, 0, 1, 1)
            (1, 1): textureProperties (e, a, b, c)
            if e == 1:
                pixels with the same alpha value as textureProperties become emissive with brightness b and alpha a
            if e > 1:
                everything is AWESOME err.. emissive with brightness b
            */
        vec4 textureProperties = texelFetch(Sampler0, ivec2(1, 1), 0);
        textureProperties.rgba *= 255;
        vec4 lmc = lightMapColor;

        if (textureProperties.r == 1) {
            if (check_alpha(color.a*255, textureProperties.a))
            {
                lmc = vec4(textureProperties.b/255);
                color.a = textureProperties.g/255;
            }
        } else if (textureProperties.r > 1) {
            lmc = vec4(1);
        }

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
