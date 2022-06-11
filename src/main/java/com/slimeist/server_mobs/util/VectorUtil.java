package com.slimeist.server_mobs.util;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class VectorUtil {
    public static Vec3f toVec3f(Vec3d v3d) {
        if (v3d == Vec3d.ZERO) {
            return Vec3f.ZERO;
        }
        return new Vec3f((float) v3d.getX(), (float) v3d.getY(), (float) v3d.getZ());
    }
    public static Vec3d toVec3d(Vec3f v3f) {
        if (v3f == Vec3f.ZERO) {
            return Vec3d.ZERO;
        }
        return new Vec3d(v3f.getX(), v3f.getY(), v3f.getZ());
    }
}
