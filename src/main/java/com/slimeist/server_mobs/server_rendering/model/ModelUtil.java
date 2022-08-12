/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package com.slimeist.server_mobs.server_rendering.model;

import net.minecraft.util.math.Vec3d;

public class ModelUtil {
    public static float interpolateAngle(float angle1, float angle2, float progress) {
        float f;
        for (f = angle2 - angle1; f < (float) (-Math.PI); f += (float) Math.PI * 2) {
        }
        while (f >= (float) Math.PI) {
            f -= (float) Math.PI * 2;
        }
        return angle1 + progress * f;
    }

    public static Vec3d rotate(Vec3d vec3d, double pitch, double yaw, double roll) {
        return rotateY(rotateX(rotateZ(vec3d, roll), pitch), yaw);
    }

    public static Vec3d rotateX(Vec3d vec3d, double angle) {
        double f = Math.cos(angle);
        double g = Math.sin(angle);
        double d = vec3d.x;
        double e = vec3d.y * f + vec3d.z * g;
        double h = vec3d.z * f - vec3d.y * g;
        return new Vec3d(d, e, h);
    }

    /**
     * Rotates vec3d vector by the given angle counterclockwise around the Y axis.
     *
     * @param angle the angle in radians
     */
    public static Vec3d rotateY(Vec3d vec3d, double angle) {
        double f = Math.cos(angle);
        double g = Math.sin(angle);
        double d = vec3d.x * f + vec3d.z * g;
        double e = vec3d.y;
        double h = vec3d.z * f - vec3d.x * g;
        return new Vec3d(d, e, h);
    }

    /**
     * Rotates vec3d vector by the given angle counterclockwise around the Z axis.
     *
     * @param angle the angle in radians
     */
    public static Vec3d rotateZ(Vec3d vec3d, double angle) {
        double f = Math.cos(angle);
        double g = Math.sin(angle);
        double d = vec3d.x * f + vec3d.y * g;
        double e = vec3d.y * f - vec3d.x * g;
        double h = vec3d.z;
        return new Vec3d(d, e, h);
    }
}

