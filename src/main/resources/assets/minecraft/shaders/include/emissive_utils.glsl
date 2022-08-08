#version 150

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

bool check_alpha(float textureAlpha, float targetAlpha) {
    float targetLess = targetAlpha - 0.01;
    float targetMore = targetAlpha + 0.01;
    return (textureAlpha > targetLess && textureAlpha < targetMore);
}

bool is_emissive(sampler2D tex) {
    return texelFetch(tex, ivec2(0, 1), 0) == vec4(1, 0, 1, 1);
}

vec4 get_texture_properties(sampler2D tex) {
    return texelFetch(tex, ivec2(1, 1), 0) * 255;
}

vec4 get_light(vec4 textureProperties, vec4 tex_color, vec4 original) {
    if (textureProperties.r == 1) {
        if (check_alpha(tex_color.a*255, textureProperties.a))
        {
            return vec4(textureProperties.b/255);
        }
    } else if (textureProperties.r > 1) {
        return vec4(textureProperties.b/255);
    }
    return original;
}

float get_alpha(vec4 textureProperties, vec4 tex_color) {
    if (textureProperties.r == 1) {
        if (check_alpha(tex_color.a*255, textureProperties.a))
        {
            return textureProperties.g/255;
        }
    }
    return tex_color.a;
}