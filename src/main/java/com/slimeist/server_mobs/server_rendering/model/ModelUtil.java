/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package com.slimeist.server_mobs.server_rendering.model;

public class ModelUtil {
    public static float interpolateAngle(float angle1, float angle2, float progress) {
        float f;
        for (f = angle2 - angle1; f < (float)(-Math.PI); f += (float)Math.PI * 2) {
        }
        while (f >= (float)Math.PI) {
            f -= (float)Math.PI * 2;
        }
        return angle1 + progress * f;
    }
}

