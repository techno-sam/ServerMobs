package com.slimeist.server_mobs.server_rendering.model;

import com.slimeist.server_mobs.ServerMobsMod;
import net.minecraft.util.math.Vec3f;

public class ScaleUtils {

    public static final double mc_max_extent = 48; //-16 to 32

    public static double getScaling(Scale stand_size) {
        return 1.6 / (stand_size.small ? 0.7 : 1);
    }

    public static float minValue(Vec3f a, Vec3f b) {
        float ax = a.getX();
        float ay = a.getY();
        float az = a.getZ();

        float bx = b.getX();
        float by = b.getY();
        float bz = b.getZ();

        float min_x = Math.min(ax, bx);
        float min_y = Math.min(ay, by);
        float min_z = Math.min(az, bz);

        return Math.min(min_x, Math.min(min_y, min_z));
    }

    public static float maxValue(Vec3f a, Vec3f b) {
        float ax = a.getX();
        float ay = a.getY();
        float az = a.getZ();

        float bx = b.getX();
        float by = b.getY();
        float bz = b.getZ();

        float max_x = Math.max(ax, bx);
        float max_y = Math.max(ay, by);
        float max_z = Math.max(az, bz);

        return Math.max(max_x, Math.max(max_y, max_z));
    }

    public static boolean needsScaling(Vec3f a, Vec3f b) {
        return minValue(a, b) < -16 || maxValue(a, b) > 32;
    }

    public static double scaleAmount(Vec3f a, Vec3f b) {
        if (!needsScaling(a, b))
            return 1;
        double neg_scale = minValue(a, b) / -16;
        double pos_scale = maxValue(a, b) / 32;
        return Math.max(neg_scale, pos_scale);
    }

    public static double maxScale(Scale stand_size) {
        return 4 / getScaling(stand_size);
    }

    public static Scale standSize(double scale_amount) {
        if (scale_amount <= maxScale(Scale.SMALL)) {
            return Scale.SMALL;
        } else if (scale_amount <= maxScale(Scale.BIG)) {
            return Scale.BIG;
        } else {
            ServerMobsMod.LOGGER.error("IMPOSSIBLE because size " + scale_amount + " exceeds maximum " + maxScale(Scale.BIG));
            return Scale.IMPOSSIBLE;
        }
    }
/*
    public static double getScaling(double extent) {
        if (fitsAtDefault(extent))
            return getScaling(Scale.SMALL);

        Scale scale = requiredScale(extent);
        if (!scale.valid)
            return -1;

        return (extent*getScaling(scale))/mc_max_extent;
    }*/

    /**
     * Get number to DIVIDE all coordinates by in model to fit.
     *//*
    public static double getCoordDivide(double extent) {
        return getScaling(extent)/getScaling(requiredScale(extent));
    }

    public static double maxExtent(Scale stand_size) {
        int maxScale = 4; //scale is capped beyond this
        return mc_max_extent * (maxScale / getScaling(stand_size));
    }

    public static double largestExtent(Vec3f a, Vec3f b) {
        //transform -16 to 32 into -24 to 24
        double ax = a.getX()-8;
        double ay = a.getY()-8;
        double az = a.getZ()-8;

        double bx = b.getX()-8;
        double by = b.getY()-8;
        double bz = b.getZ()-8;

        double x = 2 * Math.max(Math.abs(ax), Math.abs(bx)); //we have to do this because the model can't just be shifted
        double y = 2 * Math.max(Math.abs(ay), Math.abs(by));
        double z = 2 * Math.max(Math.abs(az), Math.abs(bz));

        return Math.max(x, Math.max(y, z));
    }

    public static Scale requiredScale(double extent) {
        if (extent <= maxExtent(Scale.SMALL)) {
            return Scale.SMALL;
        } else if (extent <= maxExtent(Scale.BIG)) {
            return Scale.BIG;
        } else {
            return Scale.IMPOSSIBLE;
        }
    }

    public static Scale requiredScale(Vec3f a, Vec3f b) {
        return requiredScale(largestExtent(a, b));
    }*/

    public enum Scale {
        SMALL(true, true),
        BIG(false, true),
        IMPOSSIBLE(false, false);

        public final boolean small;
        public final boolean valid;

        private Scale(boolean small, boolean valid) {
            this.small = small;
            this.valid = valid;
        }
    }
}
