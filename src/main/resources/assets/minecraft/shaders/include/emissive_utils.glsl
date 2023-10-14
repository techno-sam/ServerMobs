#version 150

bool check_alpha(float textureAlpha, float targetAlpha) {
    float targetLess = targetAlpha - 0.01;
    float targetMore = targetAlpha + 0.01;
    return (textureAlpha > targetLess && textureAlpha < targetMore);
}

// Makes sure transparent things don't become solid and vice versa.
float remap_alpha(float inputAlpha) {

    if (check_alpha(inputAlpha, 250.0/255.0)) return 1.0; // Crocodile & Wolf eyes

    return float(inputAlpha); // If a pixel doesn't need to have its alpha changed then it simply does not change.
}


vec4 make_emissive(vec4 inputColor, vec4 lightColor, float inputAlpha) {
    inputColor.a = remap_alpha(inputAlpha); // Remap the alpha value

    if (check_alpha(inputAlpha, 250.0/255.0)) return inputColor;

    return inputColor * lightColor; // If none of the pixels are supposed to be emissive, then it adds the light.
}