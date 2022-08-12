package com.slimeist.server_mobs.api.server_rendering.model;

import com.slimeist.server_mobs.api.ServerMobsApiMod;
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
            ServerMobsApiMod.LOGGER.error("IMPOSSIBLE because size " + scale_amount + " exceeds maximum " + maxScale(Scale.BIG));
            return Scale.IMPOSSIBLE;
        }
    }

    /**
     * Get number to DIVIDE all coordinates by in model to fit.
     */

    public enum Scale {
        SMALL(true, true),
        BIG(false, true),
        IMPOSSIBLE(false, false);

        public final boolean small;
        public final boolean valid;

        Scale(boolean small, boolean valid) {
            this.small = small;
            this.valid = valid;
        }
    }
}
