package com.slimeist.server_mobs.api.util;

import com.google.gson.JsonArray;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;

public class JsonUtil {
    public static JsonArray toJsonArray(Vec3i vec) {
        JsonArray array = new JsonArray();
        array.add(vec.getX());
        array.add(vec.getY());
        array.add(vec.getZ());
        return array;
    }

    public static JsonArray toJsonArray(Vec3f vec) {
        JsonArray array = new JsonArray();
        array.add(vec.getX());
        array.add(vec.getY());
        array.add(vec.getZ());
        return array;
    }
}
