package com.slimeist.server_mobs.server_rendering.model.elements;

import com.google.gson.JsonArray;

public record ModelUV(float u0, float v0, float u1, float v1, int rotation) {
    public static ModelUV ZERO = new ModelUV(0, 0, 0, 0, 0);
    public JsonArray toJsonArray(int textureWidth, int textureHeight) {
        double uScale = textureWidth/16.0d;
        double vScale = textureHeight/16.0d;
        JsonArray array = new JsonArray();
        array.add(u0/uScale);
        array.add(v0/vScale);
        array.add(u1/uScale);
        array.add(v1/vScale);
        return array;
    }
}
