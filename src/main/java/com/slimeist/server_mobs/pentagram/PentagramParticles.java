package com.slimeist.server_mobs.pentagram;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

@SuppressWarnings("SameParameterValue")
public record PentagramParticles(ServerWorld world, BlockPos centerFire) {
    public void spawn() {
        if (world == null) return;
        circle(12, 0.0, 4.0, (world.getTime()%360), 0xf92672, 2.0f);
        circle(4, 3.0, 1.5, -(world.getTime()*4%360), 0x7314b9, 1.0f);

        pentagram(4.0, (world.getTime()/4.0%360), 0xdc1b1f);

        c(3, 3);
        c(3, -3);
        c(-3, 3);
        c(-3, -3);
    }

    private void c(int x, int z) {
        linkCandle(x, 0, z, 8, (world.getTime()%40)/40.0, 0xffe400);
    }

    private void circle(int particleCount, double y, double radius, double offsetDegrees, int color, float particleSize) {
        for (int i = 0; i < particleCount; i++) {
            double degrees = i * (360.0/particleCount) + offsetDegrees;
            float radians = (float) Math.toRadians(degrees);
            double x = radius * MathHelper.cos(radians);
            double z = radius * MathHelper.sin(radians);

            world.spawnParticles(new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(color)), particleSize),
                x+centerFire.getX()+0.5, y+centerFire.getY()+0.5, z+centerFire.getZ()+0.5,
                0, 0, 1, 0, radius/2); // by setting count to 0, the particles will move with the specified speed
        }
    }

    private void pentagram(double radius, double offsetDegrees, int color) {
        double[] x_coords = new double[5];
        double[] z_coords = new double[5];
        for (int i = 0; i < 5; i++) {
            double degrees = i * 72.0 + offsetDegrees; // 360 / 5 = 72
            float radians = (float) Math.toRadians(degrees);
            x_coords[i] = radius * MathHelper.cos(radians);
            z_coords[i] = radius * MathHelper.sin(radians);
        }

        double y = 0;
        for (int i = 0; i < 5; i++) {
            int other_i = (i + 2) % 5;

            double x1 = x_coords[i];
            double z1 = z_coords[i];

            double x2 = x_coords[other_i];
            double z2 = z_coords[other_i];

            line(x1, y, z1, x2, y, z2, 5, (world.getTime()%60)/60.0, color);
        }
    }

    private void linkCandle(double x, double y, double z, int particleCount, double progressOffset, int color) {
        line(x, y, z, 0, 3.5+progressOffset, 0, particleCount, progressOffset, color);
    }

    private void line(double x1, double y1, double z1, double x2, double y2, double z2, int particleCount, double progressOffset, int color) {
        for (int i = 0; i < particleCount; i++) {
            double progress = ((double)i / particleCount) + progressOffset;
            while (progress > 1) progress--;
            while (progress < 0) progress++;

            double x = MathHelper.lerp(progress, x1, x2) + centerFire.getX() + 0.5;;
            double y = MathHelper.lerp(progress, y1, y2) + centerFire.getY() + 0.5;;
            double z = MathHelper.lerp(progress, z1, z2) + centerFire.getZ() + 0.5;;

            world.spawnParticles(new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(color)), 1.25f),
                x, y, z,
                0, 0, 1, 0, 0.5); // by setting count to 0, the particles will move with the specified speed
        }
    }
}
